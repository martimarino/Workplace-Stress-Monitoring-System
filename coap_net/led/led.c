#include "contiki.h"
#include <stdio.h>
#include "os/dev/leds.h"
#include "sys/etimer.h"

PROCESS(led, "Led prova");
AUTOSTART_PROCESSES(&led);

PROCESS_THREAD(led, ev, data) {
	static struct etimer et;

	PROCESS_BEGIN();
	etimer_set(&et, 2 * CLOCK_SECOND);
	leds_on(4);
	while (1) {
		PROCESS_YIELD();
		if (ev == PROCESS_EVENT_TIMER) {
			if (etimer_expired(&et)) {
				leds_toggle(4);
				etimer_restart(&et);
			}
		}
	}
	PROCESS_END();
}