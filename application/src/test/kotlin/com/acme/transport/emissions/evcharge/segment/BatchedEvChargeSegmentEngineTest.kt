package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEvChargeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEvChargeSegmentEngine(newRegistry())
        val item = newBatchedEvChargeSegment("code-779", "label-223")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
