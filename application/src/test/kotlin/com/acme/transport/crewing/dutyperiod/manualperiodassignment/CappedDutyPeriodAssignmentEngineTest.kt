package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDutyPeriodAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDutyPeriodAssignmentEngine(newRegistry())
        val item = newCappedDutyPeriodAssignment("code-886", "title-884")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
