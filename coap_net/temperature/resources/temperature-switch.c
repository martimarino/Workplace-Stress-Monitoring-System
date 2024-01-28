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

    size_t len = 0;
	const char *color = NULL;
	const char *mode = NULL;
	
	if((len = coap_get_query_variable(request, "color", &color))) 
    {
        if (color != NULL) {
            LOG_INFO("New color: %.*s\n", (int)len, color);

            if (strncmp(color, "r", strlen("r")) == 0) {
				leds_off(15);
                leds_on(8 + 1);
				if(isAuto)
					decTemp = true;
            } else if (strncmp(color, "g", strlen("g")) == 0) {
                leds_off(15);
                leds_on(4 + 1);
				incTemp = false;
				decTemp = false;
            } else if (strncmp(color, "b", strlen("b")) == 0) {
                leds_off(15);
                leds_on(2 + 1);
				if(isAuto)
					incTemp = true;
            }
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
			incTemp = false;
			decTemp = false;
        }
	} else {
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}
	
	printf("incTemp=%d-decTemp=%d-isAuto=%d\n", incTemp, decTemp, isAuto);

}
