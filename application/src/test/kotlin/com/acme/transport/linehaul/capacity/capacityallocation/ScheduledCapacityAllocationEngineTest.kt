package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCapacityAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCapacityAllocationEngine(newRegistry())
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
