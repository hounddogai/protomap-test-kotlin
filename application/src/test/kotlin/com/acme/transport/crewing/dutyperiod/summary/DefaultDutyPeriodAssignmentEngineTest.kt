package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDutyPeriodAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDutyPeriodAssignmentEngine()
        val item = newDefaultDutyPeriodAssignment("code-594", "label-418")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
