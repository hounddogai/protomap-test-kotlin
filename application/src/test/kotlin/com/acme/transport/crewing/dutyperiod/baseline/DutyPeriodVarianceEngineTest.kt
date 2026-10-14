package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodVarianceEngine()
        val item = newDutyPeriodVariance("code-599", "name-403")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
