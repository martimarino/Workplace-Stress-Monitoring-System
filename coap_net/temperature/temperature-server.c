#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "sys/etimer.h"
#include "dev/leds.h"
#include "os/dev/button-hal.h"
#include "global_params.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"


#define SERVER_EP           "coap://[fd00::1]:5683"
/*
#define SERVER_EP           "coap://[fd80::f6ce:367e:a8e6:6e01]:5683"
*/
#define CONN_TRY_INTERVAL   1
#define REG_TRY_INTERVAL    1

#define SENSOR_TYPE         "temperature"
#define SAMPLING_RATE       8

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE          "App"
#define LOG_LEVEL           LOG_LEVEL_APP

PROCESS(temperature_server, "Temperature sensor server");
AUTOSTART_PROCESSES(&temperature_server);

//*************************** GLOBAL VARIABLES *****************************//

char* service_url = "/registration";

static bool connected = false;
static bool registered = false;

static struct etimer wait_connectivity;
static struct etimer wait_registration;
static struct etimer simulation;

extern coap_resource_t temperature_sensor;
extern coap_resource_t temperature_switch;

//*************************** UTILITY FUNCTIONS *****************************//

static void check_connection()
{
    if (!NETSTACK_ROUTING.node_is_reachable())
    {
        LOG_WARN("BR not reachable\n");
        etimer_reset(&wait_connectivity);
    }
    else
    {
        LOG_INFO("BR reachable\n");
		leds_off(15);
        leds_on(6);			// yellow
        connected = true;
    }
}


void client_chunk_handler(coap_message_t *response)
{
    const uint8_t* chunk;

    if (response == NULL)
    {
        LOG_WARN("Request timed out\n");
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
        return;
    }
    
    int len = coap_get_payload(response, &chunk);
    
    if(strncmp((char*)chunk, "Successful Registration!", len) == 0){
        registered = true;
		leds_off(15);
        leds_set(1);	// single led
    }
    else
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
}

//*************************** THREAD *****************************//

PROCESS_THREAD(temperature_server, ev, data)
{
	button_hal_button_t *btn;
	
    PROCESS_BEGIN();

    static coap_endpoint_t server_ep;
    static coap_message_t request[1]; // This way the packet can be treated as pointer as usual
  
	leds_off(15);
    leds_set(2);		//red
    etimer_set(&wait_connectivity, CLOCK_SECOND * CONN_TRY_INTERVAL);
    
    while (!connected) {
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connectivity));
        check_connection();
		leds_toggle(2);
    }
    LOG_INFO("Temperature server connected\n");
	
	leds_off(15);
	leds_on(6);
    
    // Registration
    LOG_INFO("Sending registration message\n");
    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
    coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
    coap_set_header_uri_path(request, service_url);
    coap_set_payload(request, (uint8_t*) SENSOR_TYPE, sizeof(SENSOR_TYPE) - 1);

    while (!registered) {
        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
        // wait for the timer to expire
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
		leds_toggle(6);
    }
	
	leds_off(15);
	leds_on(4);

    LOG_INFO("Temperature server registered\n");
    LOG_INFO("Starting temperature server\n");

    // RESOURCES ACTIVATION
    coap_activate_resource(&temperature_sensor, "sensor");
    coap_activate_resource(&temperature_switch, "switch");

    // SIMULATION
    etimer_set(&simulation, CLOCK_SECOND * SAMPLING_RATE);
    LOG_INFO("Simulation start\n");
    
    while (1) {
        PROCESS_WAIT_EVENT();
        
        if (ev == PROCESS_EVENT_TIMER && data == &simulation) {
            temperature_sensor.trigger();
            etimer_set(&simulation, CLOCK_SECOND * SAMPLING_RATE);
        } 
		if (ev == button_hal_press_event) {
			btn = (button_hal_button_t *)data;
			LOG_INFO("Press event (%s)\n", BUTTON_HAL_GET_DESCRIPTION(btn));
			isAuto = !isAuto;
			incTemp = false;
			decTemp = false;
		}
    }
    
    PROCESS_END();
}