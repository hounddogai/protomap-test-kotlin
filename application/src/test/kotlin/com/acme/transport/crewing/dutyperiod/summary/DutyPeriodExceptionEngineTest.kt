package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodExceptionEngine()
        val item = newDutyPeriodException("code-179", "label-516")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
