package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class RelaySegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelaySegmentEngine()
        val item = newRelaySegment("code-239", "label-487")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
