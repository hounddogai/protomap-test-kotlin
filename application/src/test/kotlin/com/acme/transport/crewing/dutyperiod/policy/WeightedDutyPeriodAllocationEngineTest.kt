package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDutyPeriodAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDutyPeriodAllocationEngine(newRegistry())
        val item = newWeightedDutyPeriodAllocation("code-225", "label-478")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
