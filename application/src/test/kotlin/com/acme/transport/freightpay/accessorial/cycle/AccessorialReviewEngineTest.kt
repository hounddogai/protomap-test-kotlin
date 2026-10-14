package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialReviewEngine()
        val item = newAccessorialReview("code-120", "title-727")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
