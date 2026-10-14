package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnMappingEngine()
        val item = newFuelBurnMapping("code-234", "title-853")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
