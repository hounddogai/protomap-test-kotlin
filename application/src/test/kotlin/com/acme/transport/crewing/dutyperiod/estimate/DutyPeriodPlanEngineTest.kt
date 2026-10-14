package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodPlanEngine()
        val item = newDutyPeriodPlan("code-485", "label-745")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
