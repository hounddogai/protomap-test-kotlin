package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDutyPeriodSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDutyPeriodSegmentEngine(newRegistry())
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
