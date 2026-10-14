package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockReviewEngine(newRegistry())
        val item = newCrossDockReview("code-157", "title-799")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
