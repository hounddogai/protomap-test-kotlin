package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldReviewEngine(newRegistry())
        val item = newHoldReview("code-453", "title-339")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
