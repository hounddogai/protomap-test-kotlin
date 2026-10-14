package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelBurnTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelBurnTargetEngine()
        val item = newRollingFuelBurnTarget("code-518", "title-608")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
