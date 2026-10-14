package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRestBreakSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRestBreakSummaryEngine()
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
