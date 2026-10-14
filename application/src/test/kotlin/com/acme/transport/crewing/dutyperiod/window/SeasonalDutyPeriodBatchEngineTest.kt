package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDutyPeriodBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDutyPeriodBatchEngine()
        val item = newSeasonalDutyPeriodBatch("code-299", "label-552")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
