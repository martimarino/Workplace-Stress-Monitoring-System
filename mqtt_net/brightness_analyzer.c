
/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include "node-id.h"

#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <time.h>
#include <stdbool.h>
#include <time.h>
#include <math.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define LOWER_BOUND_LUM             300
#define UPPER_BOUND_LUM             500
#define VARIATION                   1

static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;

// We assume that the broker does not require authentication


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		  0
#define STATE_NET_OK    	  1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_brightness_client_process);
AUTOSTART_PROCESSES(&mqtt_brightness_client_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

// Periodic timer to check the state of the MQTT client
#define DEFAULT_STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;
static long STATE_MACHINE_PERIODIC = DEFAULT_STATE_MACHINE_PERIODIC;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;
static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_brightness_client_process, "Brightness MQTT Client");

#define MIN_BRIGHTNESS 0
#define MAX_BRIGHTNESS 100
static int brightness_level = 400;
static int mode = 0; //mode 0 = automatic mode, mode 1 = manual mode

/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
    char message[64];
    printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
           topic_len, chunk_len);
    strcpy(message, "brightness_");
    sprintf(message + strlen("brightness_"), "%d", node_id);

    if(strcmp(topic, message) == 0) {
        printf("Received Actuator command\n");
        printf("%s\n", chunk);
        if(strcmp((const char *)chunk, "inc")==0){
            printf("Turn on light, low brightness level \n");
            leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        }
        else if(strcmp((const char *)chunk, "dec")==0){
            printf("Turn off light, high brightness level \n");
            leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        }else if (strcmp((const char *)chunk, "good")==0){
            printf("Good brightness level!\n");
            leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
        }else if(strcmp((const char *)chunk, "off")==0){
            printf("Manual handling on!\n");
            leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
        }else{
            printf("UNKNOWN COMMAND\n");
        }

        return;
    }

}

static bool check_sub = false;
/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
    switch(event) {
        case MQTT_EVENT_CONNECTED: {
            printf("Application has a MQTT connection\n");

            state = STATE_CONNECTED;
            break;
        }
        case MQTT_EVENT_DISCONNECTED: {
            printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

            state = STATE_DISCONNECTED;
            process_poll(&mqtt_brightness_client_process);
            break;
        }
        case MQTT_EVENT_PUBLISH: {
            msg_ptr = data;

            pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                        msg_ptr->payload_chunk, msg_ptr->payload_length);
            break;
        }
        case MQTT_EVENT_SUBACK: {
#if MQTT_311
            mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      printf("Application is subscribed to topic successfully\n");
      check_sub = true;
    } else {
      printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
            printf("Application is subscribed to topic successfully\n");
            check_sub = true;
#endif
            break;
        }
        case MQTT_EVENT_UNSUBACK: {
            printf("Application is unsubscribed to topic successfully\n");
            break;
        }
        case MQTT_EVENT_PUBACK: {
            printf("Publishing complete.\n");
            break;
        }
        default:
            printf("Application got a unhandled MQTT event: %i\n", event);
            break;
    }
}

static bool
have_connectivity(void)
{
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
       uip_ds6_defrt_choose() == NULL) {
        return false;
    }
    return true;
}

static void
simulate_brightness(void)
{
   double frequency = 0.1;
   time_t currentTime;
    struct tm *localTime;

    time(&currentTime);
    localTime = localtime(&currentTime);

    // Translate and normalize the value of the sinusoidal function
    double normalizedValue = 0.5 * sin(2 * M_PI * frequency * localTime->tm_hour / 24.0) + 0.5;

    // Scale the value into the desired range (200-500)
    double luxValue = 200.0 + normalizedValue * (500.0 - 200.0);

    brightness_level = luxValue;
}

mqtt_status_t status;
char broker_address[CONFIG_IP_ADDR_STR_LEN];

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_brightness_client_process, ev, data)
{
button_hal_button_t* btn;
PROCESS_BEGIN();
btn = button_hal_get_by_index(0);
printf("MQTT Client Process\n");

// Initialize the ClientID as MAC address
snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

// Broker registration
mqtt_register(&conn, &mqtt_brightness_client_process, client_id, mqtt_event,
MAX_TCP_SEGMENT_SIZE);
printf("Registration done!\n");
state=STATE_INIT;

// Initialize periodic timer to check the status
etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

/* Main loop */
while(1) {

    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) ||
    ev == PROCESS_EVENT_POLL){
	printf("State %d\n", state);

        if(state==STATE_INIT){
            if(have_connectivity()==true)
                state = STATE_NET_OK;
        }

        if(state == STATE_NET_OK){
            // Connect to MQTT server
            printf("Connecting to MQTT server!\n");
            //leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
            memcpy(broker_address, broker_ip, strlen(broker_ip));

            mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
            (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
            MQTT_CLEAN_SESSION_ON);
            state = STATE_CONNECTED;
        }

        if(state==STATE_CONNECTED){
           //subscribe topic
	    int node = node_id;
	    strcpy(sub_topic, "brightness_");
            sprintf(sub_topic + strlen("brightness_"), "%d", node);
            status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

            printf("Subscribing to topic %s\n", sub_topic);
            if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                LOG_ERR("Tried to subscribe but command queue was full!\n");
                PROCESS_EXIT();

            }
	    state = STATE_SUBSCRIBED;

        }

        if(state == STATE_SUBSCRIBED){
	    static char pub_topic[BUFFER_SIZE];
            sprintf(pub_topic, "%s", "brightness_sample");

            simulate_brightness();

            PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;

            sprintf(app_buffer, "{\"node\": %d, \"brightness\": %d, \"timestamp\": %lu, \"mode\": %d}", node_id, brightness_level, clock_seconds(), mode);
            printf("%s\n", app_buffer);
            mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
            strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

            STATE_MACHINE_PERIODIC = PUBLISH_INTERVAL;

        } else if ( state == STATE_DISCONNECTED ){
            LOG_ERR("Disconnected from MQTT broker\n");
            state = STATE_INIT;
        }

        etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

}
  if(ev == button_hal_press_event) {
		btn = (button_hal_button_t *)data;
		mode = (mode == 0)? 1 : 0;
		printf("Press event (%s)\n",    BUTTON_HAL_GET_DESCRIPTION(btn));
	    }
}

PROCESS_END();
}
/*---------------------------------------------------------------------------*/