package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDutyPeriodEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDutyPeriodEstimateEngine()
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
