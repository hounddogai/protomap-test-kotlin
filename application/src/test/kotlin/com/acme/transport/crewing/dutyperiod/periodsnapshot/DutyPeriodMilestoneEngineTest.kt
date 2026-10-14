package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodMilestoneEngine(newRegistry())
        val item = newDutyPeriodMilestone("code-460", "title-860")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
