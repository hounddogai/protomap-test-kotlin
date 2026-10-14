package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelBurnThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelBurnThresholdEngine()
        val item = newRegionalFuelBurnThreshold("code-257", "label-979")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
