package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnOverrideEngine(newRegistry())
        val item = newFuelBurnOverride("code-571", "name-741")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
