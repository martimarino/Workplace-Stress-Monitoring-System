/*
 * Copyright (c) 2020, Carlo Vallati, University of Pisa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "sys/etimer.h"

#include <stdbool.h>

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

#define CONN_TRY_INTERVAL   1
#define REG_TRY_INTERVAL    1

#define SENSOR_TYPE         "temperature"
#define SAMPLING_RATE       4

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE          "App"
#define LOG_LEVEL           LOG_LEVEL_APP

extern coap_resource_t temp_sensor;
extern coap_resource_t temp_switch;


PROCESS(temperature_server, "Temperature server");
AUTOSTART_PROCESSES(&temperature_server);

//*************************** GLOBAL VARIABLES *****************************//

char* service_url = "/registration";

static bool connected = false;
static bool registered = false;

static struct etimer wait_connectivity;
static struct etimer wait_registration;
static struct etimer e_timer;

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
    
    if (strncmp((char*)chunk, "Registration successful!", len) == 0)
        registered = true;
    else
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
}

//*************************** THREAD *****************************//

PROCESS_THREAD(temperature_server, ev, data)
{
	button_hal_button_t *btn;
	
    PROCESS_BEGIN();
	
	PROCESS_PAUSE();

	LOG_INFO("Starting Temperature Server\n");

    static coap_endpoint_t server_ep;
    static coap_message_t request[1]; // packet treated as pointer
  
    leds_set(2);		//red
    etimer_set(&wait_connectivity, CLOCK_SECOND * CONN_TRY_INTERVAL);
    
    while (!connected) {
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connectivity));
        check_connection();
		etimer_reset(&wait_connectivity);
    }
	
	leds_set(1);	//single led
    
    // Registration
    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
    coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
    coap_set_header_uri_path(request, service_url);
    coap_set_payload(request, (uint8_t*) SENSOR_TYPE, sizeof(SENSOR_TYPE) - 1);

    while (!registered) {
        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
    }
	leds_on(6);		//yellow
    LOG_INFO("Temperature server registered\n");


    // Resources activation
	coap_activate_resource(&temp_sensor, "sensor");
    coap_activate_resource(&temp_switch, "switch");
	leds_set(5);	//green


    // Simulation
    etimer_set(&e_timer, CLOCK_SECOND * SAMPLING_RATE);
    LOG_INFO("Simulation start\n");
    
    while (1) {
        PROCESS_WAIT_EVENT();
        
        if (ev == PROCESS_EVENT_TIMER && data == &e_timer) {
            temp_sensor.trigger();
            etimer_set(&e_timer, CLOCK_SECOND * SAMPLING_RATE);
        } 
		if (ev == button_hal_press_event) {
			btn = (button_hal_button_t *)data;
			LOG_INFO("Press event (%s)\n", BUTTON_HAL_GET_DESCRIPTION(btn));
			isAuto = !isAuto;
		}
    }
    
    PROCESS_END();
}