package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardReviewEngine(newRegistry())
        val item = newTrailerYardReview("code-768", "label-428")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
