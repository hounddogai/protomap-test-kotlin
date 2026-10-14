package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeSegmentEngine()
        val item = newEvChargeSegment("code-994", "name-385")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
