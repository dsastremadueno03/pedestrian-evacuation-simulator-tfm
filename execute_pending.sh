#!/bin/bash

# Configuración de carpetas y parámetros masivos
PENDING_DIR="data/experiment/pending"
COMPLETED_DIR="data/experiment/completed"
JSON_EVO="data/config/numeric.json" 
NUM_RUNS=1

# Asegurar que existan las carpetas de salida
mkdir -p "$COMPLETED_DIR"
mkdir -p "data/experiment"

# Validar si hay archivos en la carpeta pending
shopt -s nullglob
EXPERIMENTOS=("$PENDING_DIR"/*.json)

if [ ${#EXPERIMENTOS[@]} -eq 0 ]; then
    echo "No se encontraron archivos .json en la carpeta $PENDING_DIR"
    exit 1
fi

echo "Encontrados ${#EXPERIMENTOS[@]} experimentos en la cola. Iniciando procesamiento..."

# BUCLE PRINCIPAL: Procesa cada archivo de la carpeta pending
for EXP_FILE in "${EXPERIMENTOS[@]}"
do
    # Extraemos solo el nombre del archivo sin ruta ni extensión
    PREFIX=$(basename "$EXP_FILE" .json)
    
    echo "[COLA] Procesando experimento: $PREFIX"
    echo "Lanzando $NUM_RUNS procesos paralelos independientes en la CPU..."

    # Lanzamos los 20 runs en paralelo para ESTE experimento concreto
    for ((i=0; i<NUM_RUNS; i++))
    do
        # Inyectamos las variables al main
		java -jar tfm.jar "$EXP_FILE" "$JSON_EVO" "$i" &
    done

    echo "Esperando a que finalicen los procesos de $PREFIX..."
    wait
    echo "Runs completados para $PREFIX."

    # FASE DE UNIFICACIÓN: Consolidar los 20 CSVs temporales
    OUTPUT_MAESTRO="data/results/results_${PREFIX}.csv"
    TEMP_PREFIX="data/results/results_${PREFIX}_run_"

    echo "Unificando archivos en el maestro: $OUTPUT_MAESTRO"

    # 1. Copiamos el run 0 entero con su cabecera para inicializar el maestro
    cp "${TEMP_PREFIX}0.csv" "$OUTPUT_MAESTRO"
    rm "${TEMP_PREFIX}0.csv"

    # 2. Concatenamos el resto de los runs (del 1 al 19) saltando su cabecera
    for ((i=1; i<NUM_RUNS; i++))
    do
        tail -n +2 "${TEMP_PREFIX}${i}.csv" >> "$OUTPUT_MAESTRO"
        rm "${TEMP_PREFIX}${i}.csv" # Limpieza en caliente
    done


    # SISTEMA DE COLAS: Mover el archivo ya procesado a completed

    echo "Moviendo archivo de configuración a la carpeta de completados..."
    mv "$EXP_FILE" "$COMPLETED_DIR/"
    
    echo "Finalizado con éxito: $PREFIX"
done

echo "COLA COMPLETADA TOTALMENTE - Todos los archivos maestros están listos."