package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnMilestoneEngine(newRegistry())
        val item = newLeaseReturnMilestone("code-426", "name-413")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
