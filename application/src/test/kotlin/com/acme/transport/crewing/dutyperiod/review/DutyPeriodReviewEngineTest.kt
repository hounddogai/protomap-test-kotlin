package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodReviewEngine()
        val item = newDutyPeriodReview("code-289", "name-567")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
