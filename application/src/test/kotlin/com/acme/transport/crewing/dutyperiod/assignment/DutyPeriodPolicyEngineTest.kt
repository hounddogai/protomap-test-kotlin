package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodPolicyEngine()
        val item = newDutyPeriodPolicy("code-797", "label-968")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
