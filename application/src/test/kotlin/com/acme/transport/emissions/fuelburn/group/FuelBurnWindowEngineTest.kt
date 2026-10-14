package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnWindowEngine(newRegistry())
        val item = newFuelBurnWindow("code-224", "title-98")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
