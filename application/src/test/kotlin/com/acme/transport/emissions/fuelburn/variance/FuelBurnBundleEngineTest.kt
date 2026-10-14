package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnBundleEngine()
        val item = newFuelBurnBundle("code-641", "title-335")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
