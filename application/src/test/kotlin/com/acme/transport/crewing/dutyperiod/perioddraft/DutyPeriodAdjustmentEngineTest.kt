package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodAdjustmentEngine(newRegistry())
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
