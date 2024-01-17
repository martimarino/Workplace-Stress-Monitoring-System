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

// Funzione per gestire le richieste GET sulla risorsa temperature_sensor
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

// Funzione per gestire le richieste PUT sulla risorsa temperature_sensor
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

// Funzione per gestire gli eventi associati alla risorsa temperature_sensor
static void temperature_event_handler(void);

// Dichiarazione della risorsa temperature_sensor come una risorsa CoAP di tipo "event"
EVENT_RESOURCE(temperature_sensor,
               "</temperature_sensor>;title=\"Temperature sensor\";rt=\"sensor\"",
               get_temperature_handler,		// get handler
               NULL,						// post handler
               put_temperature_handler,		// put handler
               NULL,						// delete handler
               temperature_event_handler);
			   
// Funzione per ottenere il timestamp corrente
char* get_current_timestamp() {
    time_t current_time;
    time(&current_time);

    struct tm *local_time = localtime(&current_time);

    char *timestamp_str = (char *)malloc(20 * sizeof(char)); // Sufficiente per una rappresentazione standard del timestamp
    strftime(timestamp_str, 20, "%Y-%m-%d %H:%M:%S", local_time);

    return timestamp_str;
}

// Gestisce le richieste GET sulla risorsa temperature_sensor
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature get request...\n");
    char* msg;
	
	// Ottieni il timestamp corrente
    char *timestamp = get_current_timestamp();

    // Se la temperatura è troppo bassa o troppo alta, invia un avviso
    if (temperature < LOWER_BOUND_TEMP) {
		
        LOG_WARN("Temperature is too low\n");
        int length = snprintf(NULL, 0, "%d %s", temperature, timestamp) + sizeof("WARN low") + 1;
        msg = (char *)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN low %d %s", temperature, timestamp);
		
    } else if (temperature > UPPER_BOUND_TEMP) {
		
        LOG_INFO("Temperature is too high\n");
        int length = snprintf(NULL, 0, "%d %s", temperature, timestamp) + sizeof("WARN high") + 1;
        msg = (char *)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN high %d %s", temperature, timestamp);
		
    } else {
		
        int length = snprintf(NULL, 0, "%d %s", temperature, timestamp) + 1;
        msg = (char *)malloc((length)*sizeof(char));
        snprintf(msg, length, "%d %s", temperature, timestamp);
		
    }
    else
    {
        //static const size_t max_char_len = 4; //-dd\0
		int length = snprintf(NULL, 0, "%d %s", temperature, timestamp) + 1;
        msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "%d %s", temperature, timestamp);
    }

    // PREPARE BUFFER
    size_t len = strlen(msg);
	memset(buffer, 0, sizeof(uint8_t));
    memcpy(buffer, (const void *)msg, len);		// copy the response in the transmission buffer
    free(msg);

    // configure CoAP rerponse
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

// Gestisce le richieste PUT sulla risorsa temperature_sensor
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature put request...\n");

    // Verifica la validità della richiesta e del payload
    if (request == NULL)
    {
        LOG_INFO("[TEMP]: Empty request or payload\n");
        return;
    }

    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;

    // Estrae e gestisce il payload della richiesta PUT
    if ((len = coap_get_payload(request, &payload)))
    {
        char* chunk = strtok((char*)payload, " ");
        char* type = (char*)malloc((strlen(chunk)) * sizeof(char));
        strcpy(type, chunk);

        chunk = strtok(NULL, " ");
        int new_value = atoi(chunk);
        printf("type: %s\n", type);

        // Aggiorna il limite superiore o inferiore in base al tipo specificato
        if (strncmp(type, "u", 1) == 0)
        {
            if (new_value < UPPER_BOUND_TEMP)
                success = false;
            else
                UPPER_BOUND_TEMP = new_value;
        }
        else
        {
            if (new_value > LOWER_BOUND_TEMP)
                success = false;
            else
                LOWER_BOUND_TEMP = new_value;
        }

        free(type);
    }

    printf("LB: %d, UB: %d\n", LOWER_BOUND_TEMP, UPPER_BOUND_TEMP);

    // Se la modifica del limite superiore o inferiore non è riuscita, imposta uno stato di risposta di errore
    if (!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

// Gestisce gli eventi associati alla risorsa temperature_sensor
static void temperature_event_handler(void)
{
	// Stima una nuova temperatura in modo casuale
    srand(time(NULL));
    int new_temp = temperature;
    int random = rand() % 8; // genera 0, 1, 2, 3, 4, 5, 6, 7

    // Modifica la temperatura con una probabilità del 25%
    if (random < 3)
    {
        if (random == 0) // diminuisci
            new_temp -= VARIATION;
        else // aumenta
            new_temp += VARIATION;
    }

    // Se la nuova temperatura è diversa dalla temperatura corrente, aggiorna e notifica gli osservatori
    if (new_temp != temperature)
    {
		LOG_INFO("new temperature: %d\n", new_temp);
        temperature = new_temp;
        coap_notify_observers(&temperature_sensor);
    }
}