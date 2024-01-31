#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <time.h>

#include "coap-engine.h"
#include "dev/leds.h"
#include "node-id.h"

#include "global_params.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/********************* RESOURCES **************************/

#define VARIATION 1

static int LOWER_BOUND_TEMP = 19;
static int UPPER_BOUND_TEMP = 27;

static int temperature = 22;

/****************** REST: Temperature *********************/

static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void event_temperature_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "title=\"Temperature sensor\";rt=\"sensor\";obs",
               get_temperature_handler,		// get handler
               NULL,						// post handler
               put_temperature_handler,		// put handler
               NULL,						// delete handler
               event_temperature_handler);
			   

// Handler for GET requests on the temperature_sensor resource
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
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
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"node_id\":%d,\"value\":%d,\"isAuto\":%s}", node_id, temperature, isAuto ? "true" : "false");
		int length = strlen((char*)buffer);

		LOG_DBG("%s\n", buffer);
//		printf("recover=%d-mode=%c\n", recover, mode);
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

// Handler for PUT requests on the temperature_sensor resource
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    LOG_INFO("Handling temperature put request...\n");

    // Verify the validity of the request
    if (request == NULL) {
        LOG_INFO("[TEMP]: Empty request\n");
        return;
    }

    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;

    // Extract and handle the payload of the PUT request
    if ((len = coap_get_payload(request, &payload))) {
        
		char* chunk = strtok((char*)payload, " ");
        char* type = (char*)malloc((strlen(chunk)+1) * sizeof(char));
        strcpy(type, chunk);

        chunk = strtok(NULL, " ");
        int new_value = atoi(chunk);

        // Update the upper or lower bound based on the specified type
		if (strncmp(type, "u", 1) == 0) {
			if (new_value <= LOWER_BOUND_TEMP) {
				success = false;
			} else {
				UPPER_BOUND_TEMP = new_value;
			}
		} else {
			if (new_value >= UPPER_BOUND_TEMP) {
				success = false;
			} else {
				LOWER_BOUND_TEMP = new_value;
			}
		}
        free(type);
    }

    LOG_DBG("LB: %d, UB: %d\n", LOWER_BOUND_TEMP, UPPER_BOUND_TEMP);
	fflush(stdout);

    // If the modification of the upper or lower bound fails, set an error response status
    if (!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

// Handler for events associated with the temperature_sensor resource
static void event_temperature_handler(void) {
	
    // Estimate a new temperature randomly
    srand(time(NULL));
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
			else // increase*/					/*
				new_temp += VARIATION;
		}
	}

    // If the new temperature is different from the current temperature, update and notify observers
    if (new_temp != temperature) {
        LOG_INFO("new temperature: %d\n", new_temp);
        temperature = new_temp;
        coap_notify_observers(&temperature_sensor);
    }
}