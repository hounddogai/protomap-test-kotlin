package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualFuelBurnThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualFuelBurnThresholdEngine()
        val item = newManualFuelBurnThreshold("code-650", "title-472")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
