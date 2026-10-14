package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnTemplateEngine(newRegistry())
        val item = newFuelBurnTemplate("code-307", "name-701")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
