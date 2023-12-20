package it.unipi.adii.iot.wsms.device;

import it.unipi.adii.iot.wsms.services.DBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import it.unipi.adii.iot.wsms.services.resources.ResRegistration;


public class SmartDevice{
	private static final int LOWER_BOUND_TEMP = 19;
	private static final int UPPER_BOUND_TEMP = 24;
	private static final Logger logger = LogManager.getLogger(SmartDevice.class);
	private static final DBService th = DBService.getInstance();

	private final String ip;
	private CoapClient resTemp;
	private CoapClient resSwitch;
	private boolean stopObserve = false;
	private short state = 0;

		
	public SmartDevice(String ipAddress) {

		this.ip = ipAddress;
		this.resTemp = new CoapClient("coap://[" + ipAddress + "]/temperature");
		this.resSwitch = new CoapClient("coap://[" + ipAddress + "]/switch");
		
		CoapObserveRelation observeTemperature = this.resTemp.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {

						long timestamp;
						int value;
						int nodeId;

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;
						
						try {
							JSONObject sensorMsg = (JSONObject) JSONValue.parseWithException(response.getResponseText());
							
							timestamp = Integer.parseInt(sensorMsg.get("timestamp").toString());
							value = Integer.parseInt(sensorMsg.get("temperature").toString());
							nodeId = Integer.parseInt(sensorMsg.get("node_id").toString());
							
						} catch (ParseException pe) {
							System.out.println(response.getResponseText());
							logger.error("Exception in parsing coap response!", pe);
							return;
						}
						
						if(ip.endsWith(Integer.toString(nodeId))) {
							if(!th.addObservation(ip, value, timestamp)) {
								logger.warn("Impossible to add new observation!");
								return;
							}
						} else {
							logger.warn("Message destination is incorrect!");
						}
						
						if (value < LOWER_BOUND_TEMP) {
							state = 1;
							String payload = "mode=off";
							Request req = new Request(Code.POST);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch?color=r");
							req.send();
							logger.info("[WARNING] - "+ip+" - the temperature ("+value+") is too low!");
							th.updateSensorState(ip, state);

						} else if(value > UPPER_BOUND_TEMP)
						{
							state = 2;
							String payload = "mode=on";
							Request req = new Request(Code.POST);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/alarm?color=r");
							req.send();
							logger.info("[CRITICAL] - "+ip+" - the temperature ("+value+") is too high!");
							th.updateSensorState(ip, state);
						} else {
							state = 0;
							String payload = "mode=on";
							Request req = new Request(Code.POST);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch?color=g");
							req.send();
							logger.info("[NORMAL] - "+ip+" - the temperature ("+value+") is comfortable!");
							th.updateSensorState(ip, state);
						}
					}
					
					public void onError() {
						stopObserve = true;
						logger.error("OBSERVING FAILED with sensor " + ip);
						
						if(ResRegistration.removeDevice(ip)) {
							th.deleteSensor(ip);
							logger.error("OBSERVING FAILED with sensor " + ip);
						} else {
							logger.error("The sensor " + ip + " is not registered");
						}
                    
					}
				}, MediaTypeRegistry.APPLICATION_JSON);
		
		if (stopObserve) {
			observeTemperature.proactiveCancel();
		}
	}

	public String getIP() {
		return ip;
	}

}
