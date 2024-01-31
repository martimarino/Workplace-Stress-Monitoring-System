#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

#include "dev/leds.h"
#include "sys/log.h"
#include <stdbool.h>

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/*          RESOURCES            */
bool isAuto = true;
int recoverLevel = 0;

/*          HANDLERS          */
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


EVENT_RESOURCE(temp_switch,
               "</temperature_switch>;title=\"Temperature switch\";rf=\"switch\"",
               NULL,					//get
               NULL,					//post
               put_switch_handler,		//put
               NULL,					//delete
               NULL);					//handler


static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	
    size_t len = 0;
	const char *color = NULL;
	const char *mode = NULL;
	
	if((len = coap_get_query_variable(request, "color", &color))) 
    {
        if (color != NULL) {
            LOG_INFO("New color: %.*s\n", (int)len, color);

            if (strncmp(color, "r", strlen("r")) == 0) {
				leds_set(2);
				if(isAuto)
					recoverLevel = -1;
            } else if (strncmp(color, "g", strlen("g")) == 0) {
                leds_set(4);
				recoverLevel = 0;
            } else if (strncmp(color, "b", strlen("b")) == 0) {
                leds_set(8);
				if(isAuto)
					recoverLevel = 1;
            }
			if(isAuto)
				leds_on(1);
        }
	} else if((len = coap_get_query_variable(request, "mode", &mode))) 
    {
        if (mode != NULL) {
            LOG_INFO("New mode: %.*s\n", (int)len, mode);

            if (strncmp(mode, "man", strlen("man")) == 0) {
                leds_off(1);

            } else if (strncmp(mode, "auto", strlen("auto")) == 0) {
                leds_on(1);
            }
			recoverLevel = 0;
        }
	} else {
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}

}