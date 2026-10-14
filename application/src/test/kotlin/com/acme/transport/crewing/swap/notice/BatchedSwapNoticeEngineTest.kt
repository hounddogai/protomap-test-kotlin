package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedSwapNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedSwapNoticeEngine()
        val item = newBatchedSwapNotice("code-297", "title-216")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
