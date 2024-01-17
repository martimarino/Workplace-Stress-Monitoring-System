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

/*          HANDLERS          */
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(temperature_switch,
               "</temperature_switch>;title=\"Temperature switch\";rt=\"switch\"",
               NULL,
               NULL,
               put_switch_handler,
               NULL,
               NULL);

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling switch put request...\n");

    size_t len = 0;
    const char *color = NULL;
    const char *mode = NULL;
    bool success = true;
	
	if ((len = coap_get_query_variable(request, "color", &color)))
    {
        LOG_DBG("color %s\n", color);

        if(strncmp(color, "r", len) == 0) 
        {
            leds_set(8+1);
        } 
		else if(strncmp(color, "g", len) == 0) 
        {
            leds_set(4+1);
        } 
		else if(strncmp(color, "b", len) == 0) 
        {
            leds_on(2+1);
        }
	}
	else if((len = coap_get_query_variable(request, "mode", &mode)))
    {
		LOG_DBG("mode %s\n", mode);

        if(strncmp(mode, "auto", len) == 0) 
        {
            isAuto = true;
			leds_set(1);
            LOG_INFO("Switched to automatic mode\n");
        }
        if(strncmp(mode, "man", len) == 0) 
        {
            isAuto = false;
			leds_set(0);
            LOG_INFO("Switched to manual mode\n");
        }
    } else {
        success = false;
	}
	
	if(!success) 
    {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
