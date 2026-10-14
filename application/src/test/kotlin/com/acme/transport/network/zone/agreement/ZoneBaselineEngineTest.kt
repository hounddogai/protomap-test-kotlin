package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneBaselineEngine()
        val item = newZoneBaseline("code-959", "label-241")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
