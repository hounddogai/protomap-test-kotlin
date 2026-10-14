package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelBurnScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelBurnScoreEngine(newRegistry())
        val item = newRegionalFuelBurnScore("code-522", "name-201")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
