package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelBurnReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelBurnReviewEngine()
        val item = newRegionalFuelBurnReview("code-760", "title-218")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
