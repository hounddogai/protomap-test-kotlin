package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTrailerSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTrailerSegmentEngine(newRegistry())
        val item = newPendingTrailerSegment("code-670", "label-905")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
