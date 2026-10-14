package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnAllocationEngine(newRegistry())
        val item = newLeaseReturnAllocation("code-65", "title-914")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
