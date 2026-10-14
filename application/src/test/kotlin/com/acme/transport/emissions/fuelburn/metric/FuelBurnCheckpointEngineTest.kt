package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnCheckpointEngine()
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
