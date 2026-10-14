package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelBurnRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelBurnRequestEngine()
        val item = newRollingFuelBurnRequest("code-696", "label-270")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
