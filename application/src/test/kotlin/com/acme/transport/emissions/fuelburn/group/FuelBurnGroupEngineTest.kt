package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnGroupEngine(newRegistry())
        val item = newFuelBurnGroup("code-161", "name-833")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
