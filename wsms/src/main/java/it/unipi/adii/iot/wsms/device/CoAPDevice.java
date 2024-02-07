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

import it.unipi.adii.iot.wsms.services.resources.ResourceRegistration;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import static it.unipi.adii.iot.wsms.services.DBService.db_Service;
import static it.unipi.adii.iot.wsms.utils.Parameters.*;


public class CoAPDevice {

	public static final Logger logger = LogManager.getLogger(CoAPDevice.class);

	private final String ip;
	private String mode = "auto";
	private int recoverLevel = 0;

	private CoapClient resSensor;
	private CoapClient resSwitch;
	private boolean stopObserve = false;



	public CoAPDevice(String ipAddress, String dataType) {

		this.ip = ipAddress;
		this.resSensor = new CoapClient("coap://[" + ipAddress + "]/sensor");
		this.resSwitch = new CoapClient("coap://[" + ipAddress + "]/switch");

		CoapObserveRelation observeProperty = this.resSensor.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {

						int nodeId = 0;
						int value = 0;
						boolean isAuto = true;

						boolean success = true;

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;

						try {
							JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(response.getPayload()));

							nodeId = Integer.parseInt(sensorMessage.get("node_id").toString());
							value = Integer.parseInt(sensorMessage.get("value").toString());
							isAuto = Boolean.parseBoolean(sensorMessage.get("isAuto").toString());

						} catch (ParseException pe) {
							System.out.println(response.getResponseText());
							logger.error("Impossible to parse the response!", pe);
							success = false;
						}

						if(ip.endsWith(Integer.toString(nodeId))) {
							if(!DBService.addObservation(ip, value)) {
								logger.warn("Impossible to add new observation!");
								success = false;
							}
						} else {
							logger.warn("Message destination is incorrect!");
						}

						if(!success)
							return;

						System.out.println("["+nodeId+"]: "+value+" °C - auto:"+isAuto+"RL: "+recoverLevel);

						// request for warn message
						if (value < getLowerBound(dataType) && recoverLevel == 0 && isAuto) {
							if(isAuto)
								recoverLevel = 1;
							logger.warn(dataType + " too low! (" + value + ")");
							Request req = new Request(Code.PUT);
							req.setURI("coap://[" + ip + "]/switch?color=b");
							req.send();
							System.out.println("Sent PUT color b to switch");

						} else if (value > getUpperBound(dataType) && recoverLevel == 0 && isAuto) {
							if (isAuto)
								recoverLevel = -1;
							logger.warn(dataType + " too high! (" + value + ")");
							Request req = new Request(Code.PUT);
							req.setURI("coap://[" + ip + "]/switch?color=r");
							req.send();
							System.out.println("Sent PUT color r to switch");
						} else if (recoverLevel != 0 && value == getComfortValue(dataType) && isAuto) {
							recoverLevel = 0;
							logger.info(dataType + " at normal level. (" + value + ")");
							Request req = new Request(Code.PUT);
							req.setURI("coap://[" + ip + "]/switch?color=g");
							req.send();
							System.out.println("Sent PUT color g to switch");
						}
						DBService.addObservation(ip, value);

						// request for mode changed
						if(isAuto && mode.equals("man")) {

							logger.info(dataType + " mode changed to: " + mode);
							Request req = new Request(Code.PUT);
							req.setURI("coap://[" + ip + "]/switch?mode=auto");
							req.send();
							mode = "auto";
							System.out.println("Sent PUT mode auto to switch");
						} else if (!isAuto && mode.equals("auto")) {
							recoverLevel = 0;
							logger.info(dataType + " mode changed to: " + mode);
							Request req = new Request(Code.PUT);
							req.setURI("coap://[" + ip + "]/switch?mode=man");
							req.send();
							mode = "man";
							System.out.println("Sent PUT mode man to switch");
						}
					}

					// System.out.println("RL set to: "+recoverLevel);

					public void onError() {
						stopObserve = true;
						logger.error("OBSERVING FAILED with " + dataType + " sensor " + ip);

						if (ResourceRegistration.removeDevice(ip)) {
							db_Service.deleteSensor(ip, dataType);
							logger.error("OBSERVING FAILED with {} sensor {}", dataType, ip);
						} else {
							logger.error("The sensor {} is not registered", ip);
						}

					}
				}, MediaTypeRegistry.APPLICATION_JSON);

		if (stopObserve) {
			observeProperty.proactiveCancel();
		}
	}

	public String getIP() {
		return ip;
	}

}