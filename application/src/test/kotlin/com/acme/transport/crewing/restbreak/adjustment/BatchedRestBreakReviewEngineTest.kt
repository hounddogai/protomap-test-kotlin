package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRestBreakReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRestBreakReviewEngine(newRegistry())
        val item = newBatchedRestBreakReview("code-508", "label-777")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
