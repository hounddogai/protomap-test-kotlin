package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCutoffRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCutoffRecordEngine()
        val item = newAdaptiveCutoffRecord("code-223", "title-530")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
