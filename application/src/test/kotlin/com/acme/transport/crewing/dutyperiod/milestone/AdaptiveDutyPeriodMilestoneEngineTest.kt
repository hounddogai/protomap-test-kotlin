package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDutyPeriodMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDutyPeriodMilestoneEngine(newRegistry())
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
