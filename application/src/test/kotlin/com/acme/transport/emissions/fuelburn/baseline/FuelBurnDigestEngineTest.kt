package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnDigestEngine()
        val item = newFuelBurnDigest("code-615", "name-919")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
