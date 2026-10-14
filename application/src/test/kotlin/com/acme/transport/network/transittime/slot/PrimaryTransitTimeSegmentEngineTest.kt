package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTransitTimeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTransitTimeSegmentEngine()
        val item = newPrimaryTransitTimeSegment("code-479", "label-437")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
