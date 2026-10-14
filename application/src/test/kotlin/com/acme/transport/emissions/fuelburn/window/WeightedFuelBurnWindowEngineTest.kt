package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnWindowEngine()
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
