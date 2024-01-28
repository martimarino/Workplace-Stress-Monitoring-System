#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/*          RESOURCES            */
bool isAuto = true;
bool incTemp = false;
bool decTemp = false;

/*          HANDLERS          */
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


EVENT_RESOURCE(temperature_switch,
               "</temperature_switch>;title=\"Temperature switch\";rf=\"switch\"",
               NULL,					// get
               NULL,					// post
               put_switch_handler,		// put
               NULL,					// delete
               NULL);


static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    LOG_INFO("Handling switch put request...\n");
	printf("[P]incTemp=%d-decTemp=%d-isAuto=%d\n", incTemp, decTemp, isAuto);

    size_t len = 0;
	const char *color = NULL;
	const char *mode = NULL;
	
	if((len = coap_get_query_variable(request, "color", &color))) 
    {
        if (color != NULL) {
            LOG_INFO("New color: %.*s\n", (int)len, color);

            if (strncmp(color, "r", strlen("r")) == 0) {
                leds_set(8 + 1);
				decTemp = true;
            } else if (strncmp(color, "g", strlen("g")) == 0) {
                leds_set(4 + 1);
				incTemp = false;
				decTemp = false;
            } else if (strncmp(color, "b", strlen("b")) == 0) {
                leds_set(2 + 1);
				incTemp = true;
            }
        }
	} else if((len = coap_get_query_variable(request, "mode", &mode))) 
    {
        if (mode != NULL) {
            LOG_INFO("New mode: %.*s\n", (int)len, mode);

            if (strncmp(mode, "man", strlen("man")) == 0) {
                isAuto = false;

            } else if (strncmp(mode, "auto", strlen("auto")) == 0) {
                isAuto = true;
            }
        }
	} else {
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}

}




/*
    if ((len = coap_get_payload(request, &payload))) {
        // convert to string
        char payload_str[len + 1];
        memcpy(payload_str, payload, len);
        payload_str[len] = '\0';  
		
		LOG_INFO("Payload: %s\n", payload_str);


        // split commands
        char* token = strtok(payload_str, "&");
        char* color = NULL;
        char* mode = NULL;

        while (token != NULL) {
            if (strncmp(token, "color=", strlen("color=")) == 0) {
                color = token + strlen("color=");
            } else if (strncmp(token, "mode=", strlen("mode=")) == 0) {
                mode = token + strlen("mode=");
            }
			// next
            token = strtok(NULL, "&");
        }

        if (color != NULL) {
            LOG_INFO("Color: %s\n", color);

            if (strncmp(color, "r", strlen("r")) == 0) {
                leds_set(8 + 1);
				decTemp = true;
            } else if (strncmp(color, "g", strlen("g")) == 0) {
                leds_set(4 + 1);
				incTemp = false;
				decTemp = false;
            } else if (strncmp(color, "b", strlen("b")) == 0) {
                leds_set(2 + 1);
				incTemp = true;
            }
        }

        if (mode != NULL) {
            LOG_INFO("Mode: %s\n", mode);

            if (strncmp(mode, "man", strlen("man")) == 0) {
                isAuto = false;

            } else if (strncmp(mode, "auto", strlen("auto")) == 0) {
                isAuto = true;
            }
        }
		printf("[D]incTemp=%d-decTemp=%d-isAuto=%d\n", incTemp, decTemp, isAuto);


    } else {
        success = false;
    }
	
	if(!success) 
    {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }*/