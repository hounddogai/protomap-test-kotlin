package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNearMissSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNearMissSegmentEngine()
        val item = newBatchedNearMissSegment("code-267", "name-703")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
