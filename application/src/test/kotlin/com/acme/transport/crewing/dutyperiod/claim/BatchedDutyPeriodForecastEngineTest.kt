package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDutyPeriodForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDutyPeriodForecastEngine()
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
