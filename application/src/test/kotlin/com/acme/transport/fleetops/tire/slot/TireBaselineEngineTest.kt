package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class TireBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireBaselineEngine()
        val item = newTireBaseline("code-293", "title-499")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
