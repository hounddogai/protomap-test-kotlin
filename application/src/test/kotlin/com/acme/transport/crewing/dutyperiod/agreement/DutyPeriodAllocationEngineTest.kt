package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodAllocationEngine(newRegistry())
        val item = newDutyPeriodAllocation("code-216", "name-711")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
