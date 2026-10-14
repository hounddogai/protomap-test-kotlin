package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyFuelBurnDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyFuelBurnDraftEngine()
        val item = newLegacyFuelBurnDraft("code-398", "name-769")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
