package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultSortationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultSortationReviewEngine()
        val item = newDefaultSortationReview("code-217", "label-588")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
