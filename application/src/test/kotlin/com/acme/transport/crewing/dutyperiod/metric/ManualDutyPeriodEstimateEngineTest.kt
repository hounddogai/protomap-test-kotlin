package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDutyPeriodEstimateEngine()
        val item = newManualDutyPeriodEstimate("code-82", "name-617")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
