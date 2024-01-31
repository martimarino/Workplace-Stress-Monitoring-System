#!/bin/bash

project_directory="../coap_net/temperature/"

# Rimuovi la directory 'build' e pulisci il progetto
cd "$project_directory" || exit
sudo rm -rf build
make clean

# Definisci le porte ACM disponibili
available_ports=("/dev/ttyACM0" "/dev/ttyACM1" "/dev/ttyACM2")

# Controlla se è stato fornito un argomento da linea di comando
if [ "$#" -ne 1 ]; then
  echo "Utilizzo: $0 <numero_porta>"
  echo "Esempio: $0 0"
  exit 1
fi

# Seleziona la porta ACM in base all'argomento fornito
selected_port="${available_ports[$1]}"

# Verifica che la porta selezionata esista
if [ ! -e "$selected_port" ]; then
  echo "La porta selezionata non esiste: $selected_port"
  exit 1
fi

# Effettua il flash sul dispositivo selezionato
make TARGET=nrf52840 BOARD=dongle temperature-server.dfu-upload PORT=$port
