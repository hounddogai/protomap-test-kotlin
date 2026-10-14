package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyFuelBurnEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyFuelBurnEntryEngine(newRegistry())
        val item = newLegacyFuelBurnEntry("code-16", "name-707")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
