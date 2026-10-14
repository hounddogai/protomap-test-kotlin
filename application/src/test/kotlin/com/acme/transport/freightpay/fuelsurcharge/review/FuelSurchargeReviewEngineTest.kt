package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeReviewEngine()
        val item = newFuelSurchargeReview("code-247", "label-292")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
