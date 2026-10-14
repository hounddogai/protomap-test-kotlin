package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnSlotEngine()
        val item = newFuelBurnSlot("code-871", "name-63")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
