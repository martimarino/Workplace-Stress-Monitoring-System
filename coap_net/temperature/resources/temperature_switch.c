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
bool inc_temp = false;
bool dec_temp = false;

/*          HANDLERS          */
static void post_put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(temperature_switch,
               "</temperature_switch>;title=\"Temperature switch\";rt=\"switch\"",
               NULL,
               NULL,
               post_put_switch_handler,
               post_put_switch_handler,
               NULL);

static void post_put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling switch put request...\n");

    size_t len = 0;
    const char *color = NULL;
	const char *recover = NULL;
    const char *mode = NULL;
    bool success = true;
	
	if((len = coap_get_query_variable(request, "mode", &mode)))
    {
		LOG_DBG("mode %s\n", mode);

        if(strncmp(mode, "auto", len) == 0) 
        {
			leds_set(1);
            LOG_INFO("Switched to automatic mode\n");
			isAuto = true;
        }
        else if(strncmp(mode, "man", len) == 0) 
        {
			leds_set(0);
            LOG_INFO("Switched to manual mode\n");
			isAuto = false;
        }
	} else {
	
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
				leds_set(2+1);
			}
		} else {
			success = false;
		} 
		if(success && (len = coap_get_post_variable(request, "recover", &recover))) 
		{
			LOG_DBG("recover %s\n", recover);

			if(strncmp(recover, "inc", len) == 0) 
			{
				inc_temp = true;
				
			} else if(strncmp(recover, "dec", len) == 0) 
			{
				dec_temp = true;
			} 
		} 
    } else {
        success = false;
	}
	
	if(!success) 
    {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
