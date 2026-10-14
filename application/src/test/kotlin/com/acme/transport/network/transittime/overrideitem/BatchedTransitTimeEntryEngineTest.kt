package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTransitTimeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTransitTimeEntryEngine()
        val item = newBatchedTransitTimeEntry("code-724", "title-847")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
