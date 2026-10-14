package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAccessorialReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAccessorialReviewEngine()
        val item = newScheduledAccessorialReview("code-511", "label-460")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
