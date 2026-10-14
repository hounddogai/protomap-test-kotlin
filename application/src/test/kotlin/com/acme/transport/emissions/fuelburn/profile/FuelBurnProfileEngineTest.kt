package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnProfileEngine()
        val item = newFuelBurnProfile("code-672", "label-486")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
