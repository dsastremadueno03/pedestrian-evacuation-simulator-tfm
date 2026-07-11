package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FitnessLogicTest {
	
	@Test
	void checkFitness() {
        double fitnessEsperado = 2.5086;
     // 1. CONFIGURACIÓN INICIAL DEL MAPA 
        double maxDiameter = 40.0;         // El supermercado mide 40 metros de diagonal
        double maxSimulationTime = 600.0;  // Tiempo límite de 10 minutos (600 segundos)

        // 2. CASO DE PRUEBA
        double muertos = 2.0;
        double atrapados = 5.0;
        double distanciaMediaALaSalida = 32.0; // En metros (32 / 40 = 0.8 normalizado)
        double tiempoMedioEvacuacion = 360.0;  // En segundos (360 / 600 = 0.6 normalizado)

        // 3. EJECUCIÓN DE TU LÓGICA EN CASCADA
        double fitnessFinal = calcularCascada(muertos, atrapados, distanciaMediaALaSalida, tiempoMedioEvacuacion, maxDiameter, maxSimulationTime);
		assertEquals(fitnessFinal, fitnessEsperado);
	}

    public static double calcularCascada(double civDead, double civTrapped, double avgDistToExit, double meanEvacuationTime, double maxDiameter, double maxSimulationTime) {
        
        // NIVEL 4: Tiempo medio de evacuación
        double levelTime = meanEvacuationTime / maxSimulationTime; // 360 / 600 = 0.6

        // NIVEL 3: Distancia media + Tiempo incrustado en su cola decimal
        double distNormalizada = avgDistToExit / maxDiameter; // 32 / 40 = 0.8
        double levelDist = distNormalizada + (levelTime / 10.0); // 0.8 + 0.06 = 0.86

        // NIVEL 2: Atrapados + Distancia/Tiempo incrustados en su cola decimal
        double levelTrapped = civTrapped + (levelDist / 10.0); // 5 + 0.086 = 5.086

        // NIVEL 1: Muertos + Todo el bloque decimal empujado a la derecha
        double fitness = civDead + (levelTrapped / 10.0); // 2 + 0.5086 = 2.5086
             
        return fitness;
    }
}