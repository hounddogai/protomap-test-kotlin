package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDutyPeriodScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDutyPeriodScheduleEngine(newRegistry())
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
