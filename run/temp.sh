#!/bin/bash

project_directory="../coap_net/rpl-border-router/"

# Controlla se è stato fornito un argomento da linea di comando
if [ "$#" -ne 1 ]; then
  echo "Utilizzo: $0 <numero_porta>"
  echo "Esempio: $0 0"
  exit 1
fi

# Seleziona la porta ACM in base all'argomento fornito
selected_port="/dev/ttyACM$1"

# Verifica che la porta selezionata esista
if [ ! -e "$selected_port" ]; then
  echo "La porta selezionata non esiste: $selected_port"
  exit 1
fi

# Naviga alla directory del progetto
cd "$project_directory" || exit

# Esegui il comando con la porta selezionata
make login TARGET=nrf52840 BOARD=dongle PORT=$selected_port