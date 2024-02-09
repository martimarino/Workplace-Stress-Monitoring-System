#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "coap-engine.h"
#include "node-id.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

#include "global_params.h"

static void temp_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_event_handler(void);

EVENT_RESOURCE(temp_sensor,
         "title=\"Temperature sensor\";rt=\"sensor\"",
         temp_get_handler,		//get
         NULL,					//post
         NULL,					//put
         NULL, 					//delete
		 temp_event_handler);	//handler

#define VARIATION 1

static int LOWER_BOUND_TEMP = 19;
static int UPPER_BOUND_TEMP = 25;

static int temperature = 22;


static void
temp_event_handler(void)
{
	// Estimate a new temperature randomly
    int new_temp = temperature;
	
	if(recoverLevel != 0) {
		if (recoverLevel == 1) {
			new_temp += VARIATION;
		} 
		else if (recoverLevel == -1) {
			new_temp -= VARIATION;
		} 
	} else {	
		int random = rand() % 8; // generates 0, 1, 2, 3, 4, 5, 6, 7
		// Change the temperature with a certain probability
		if (random < 3) {
			if (random == 0) // decrease
				new_temp -= VARIATION;
			else // increase	
				new_temp += VARIATION;
		}
	}
	
    // If the new temperature is different from the current temperature, update and notify observers
    if (new_temp != temperature) {
        LOG_INFO("new temperature: %d\n", new_temp);
        temperature = new_temp;
        coap_notify_observers(&temp_sensor);
    }
}


// Handler for GET requests on the temperature_sensor resource
static void temp_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    unsigned int accept = -1;
    coap_get_header_accept(request, &accept);

    if(accept == -1 || accept == APPLICATION_JSON) {

		if (temperature < LOWER_BOUND_TEMP)
		{
			LOG_WARN("Temperature is too low: (%d)\n", temperature);
		}
		else if (temperature > UPPER_BOUND_TEMP)
		{
			LOG_WARN("Temperature is too high: (%d)\n", temperature);
		} 
		else {
			LOG_INFO("Temperature is normal: (%d)\n", temperature);
		}

		// Fill the buffer
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"node\":%d,\"value\":%d,\"isAuto\":%s}", node_id, temperature, isAuto ? "true" : "false");
		int length = strlen((char*)buffer);

		LOG_INFO("%s, RL=%d\n", buffer, recoverLevel);
		fflush(stdout);

		// Set CoAP response msg
		coap_set_header_content_format(response, APPLICATION_JSON);
		coap_set_header_etag(response, (uint8_t *)&length, 1);
		coap_set_payload(response, buffer, length);
	}
    else
    {
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        sprintf((char *)buffer, "Supported content-types:application/json");
	    coap_set_payload(response, buffer, strlen((char*)buffer));
	}
}
