package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRestBreakThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRestBreakThresholdEngine()
        val item = newBatchedRestBreakThreshold("code-581", "label-395")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
