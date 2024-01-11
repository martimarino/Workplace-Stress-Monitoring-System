#include <stdlib.h>
#include <time.h>
#include <string.h>

#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/**************** RESOURCES **********************/
#include "global_params.h"

#define VARIATION 1

static int LOWER_BOUND_TEMP = 19;
static int UPPER_BOUND_TEMP = 27;

static int temperature = 22;


/**************** REST: Temperature **********************/
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temperature_event_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "</temperature_sensor>;title=\"Temperature sensor\";obs",
               get_temperature_handler,
               put_temperature_handler,
               put_temperature_handler,
               NULL,
               temperature_event_handler);

static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature get request...\n");
    char* msg;
    
    // IF TOO HOT OR TOO COLD SEND A WARNING
    if (temperature < LOWER_BOUND_TEMP)
    {
        LOG_WARN("Temperature is too low\n");
        int length = snprintf(NULL, 0,"%d", temperature) + sizeof("WARN cold") + 1;
        msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN cold %d", temperature);
    }
    else if (temperature > UPPER_BOUND_TEMP)
    {
        LOG_INFO("Temperature is too high\n");
        int length = snprintf(NULL, 0,"%d", temperature) + sizeof("WARN hot") + 1;
        msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN hot %d", temperature);
    }
    else
    {
        static const size_t max_char_len = 4; //-dd\0
        msg = (char*)malloc((max_char_len)*sizeof(char));
        snprintf(msg, max_char_len, "%d", temperature);
    }
    
    // PREPARE BUFFER
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    free(msg);
	
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature put request...\n");
	
	if(request == NULL){
		LOG_INFO("[HUM]: Empty request\n");
		return;
	}
    
    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        char* chunk = strtok((char*)payload, " ");
        char* type = (char*)malloc((strlen(chunk))*sizeof(char));
        strcpy(type, chunk);
        
        chunk = strtok(NULL, " ");
        int new_value = atoi(chunk);
        printf("type: %s\n", type);
        if (strncmp(type, "u", 1)==0)
        {
            if (new_value < LOWER_BOUND_TEMP)
                success = false;
            else
                UPPER_BOUND_TEMP = new_value;
        }
        else // update the lower bound
        {
            if (new_value > UPPER_BOUND_TEMP)
                success = false;
            else
                LOWER_BOUND_TEMP = new_value;
        }
        free(type);
    }
    printf("LB: %d, UB: %d\n", LOWER_BOUND_TEMP, UPPER_BOUND_TEMP);

    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

static void temperature_event_handler(void)
{
    if (!isActive) {
        return; // DOES NOTHING SINCE IS SWITCHED OFF
    }

    // extimate new temperature
    srand(time(NULL));
    int new_temp = temperature;
    int random = rand() % 8; // generate 0, 1, 2, 3, 4, 5, 6, 7
    
    if (random < 2) {// 25% of changing the value
        if (random == 0) // decrease
            new_temp -= VARIATION;
        else // increase
            new_temp += VARIATION;
    }

    // if not equal
    if (new_temp != temperature)
    {
        temperature = new_temp;
        coap_notify_observers(&temperature_sensor);
    }
}
