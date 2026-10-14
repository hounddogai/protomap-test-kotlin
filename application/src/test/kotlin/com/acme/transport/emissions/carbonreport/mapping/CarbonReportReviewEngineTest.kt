package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportReviewEngine()
        val item = newCarbonReportReview("code-58", "label-877")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
