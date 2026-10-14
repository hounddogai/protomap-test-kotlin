package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodSegmentEngine(newRegistry())
        val item = newDutyPeriodSegment("code-956", "title-626")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
