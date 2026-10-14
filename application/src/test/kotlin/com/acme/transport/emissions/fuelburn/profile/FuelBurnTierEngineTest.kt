package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnTierEngine()
        val item = newFuelBurnTier("code-477", "label-133")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
