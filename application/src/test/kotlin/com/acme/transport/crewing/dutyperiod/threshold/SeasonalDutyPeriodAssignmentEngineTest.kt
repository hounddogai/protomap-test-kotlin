package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDutyPeriodAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDutyPeriodAssignmentEngine(newRegistry())
        val item = newSeasonalDutyPeriodAssignment("code-793", "name-974")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
