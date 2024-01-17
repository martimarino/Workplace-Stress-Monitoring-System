#include <stdlib.h>
#include <time.h>
#include <string.h>

#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/********************* RESOURCES **************************/
#include "global_params.h"

#define VARIATION 1

static int LOWER_BOUND_TEMP = 19;
static int UPPER_BOUND_TEMP = 27;

static int temperature = 22;

/****************** REST: Temperature *********************/

static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temperature_event_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "</temperature_sensor>;title=\"Temperature sensor\";rt=\"sensor\"",
               get_temperature_handler,		// get handler
               NULL,						// post handler
               put_temperature_handler,		// put handler
               NULL,						// delete handler
               temperature_event_handler);
			   
		
// Function to get the current timestamp		
char* get_current_timestamp() {
    time_t current_time;
    time(&current_time);

    struct tm *local_time = localtime(&current_time);

    char *timestamp_str = (char *)malloc(20 * sizeof(char)); 
    strftime(timestamp_str, 20, "%Y-%m-%d %H:%M:%S", local_time);

    return timestamp_str;
}

// Handler for GET requests on the temperature_sensor resource
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    LOG_INFO("Handling temperature get request...\n");

    char *timestamp = get_current_timestamp();

    char* json_message;
    nsprintf(&json_message, sizeof(json_message), "{\"timestamp\": %s, \"value\": %d}", timestamp, temperature);

    free(timestamp);
	free(json_message);

    // Configure CoAP response with JSON in payload
    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_payload(response, (uint8_t *)json_message, strlen(json_message));
}

// Handler for PUT requests on the temperature_sensor resource
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    LOG_INFO("Handling temperature put request...\n");

    // Verify the validity of the request
    if (request == NULL) {
        LOG_INFO("[TEMP]: Empty request or payload\n");
        return;
    }

    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;

    // Extract and handle the payload of the PUT request
    if ((len = coap_get_payload(request, &payload))) {
        char* chunk = strtok((char*)payload, " ");
        char* type = (char*)malloc((strlen(chunk)) * sizeof(char));
        strcpy(type, chunk);

        chunk = strtok(NULL, " ");
        int new_value = atoi(chunk);
        printf("type: %s\n", type);

        // Update the upper or lower bound based on the specified type
        if (strncmp(type, "u", 1) == 0) {
            if (new_value < UPPER_BOUND_TEMP)
                success = false;
            else
                UPPER_BOUND_TEMP = new_value;
        } else {
            if (new_value > LOWER_BOUND_TEMP)
                success = false;
            else
                LOWER_BOUND_TEMP = new_value;
        }

        free(type);
    }

    printf("LB: %d, UB: %d\n", LOWER_BOUND_TEMP, UPPER_BOUND_TEMP);

    // If the modification of the upper or lower bound fails, set an error response status
    if (!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

// Handler for events associated with the temperature_sensor resource
static void temperature_event_handler(void) {
	
    // Estimate a new temperature randomly
    srand(time(NULL));
    int new_temp = temperature;
    int random = rand() % 8; // generates 0, 1, 2, 3, 4, 5, 6, 7

    // Change the temperature with a certain probability
    if (random < 3) {
        if (random == 0) // decrease
            new_temp -= VARIATION;
        else // increase
            new_temp += VARIATION;
    }

    // If the new temperature is different from the current temperature, update and notify observers
    if (new_temp != temperature) {
        LOG_INFO("new temperature: %d\n", new_temp);
        temperature = new_temp;
        coap_notify_observers(&temperature_sensor);
    }
}