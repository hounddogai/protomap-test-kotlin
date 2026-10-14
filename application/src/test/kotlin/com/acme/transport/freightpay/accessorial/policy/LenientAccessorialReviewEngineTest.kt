package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAccessorialReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAccessorialReviewEngine(newRegistry())
        val item = newLenientAccessorialReview("code-584", "name-434")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
