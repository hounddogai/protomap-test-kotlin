package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakReviewEngine(newRegistry())
        val item = newRestBreakReview("code-803", "title-577")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
