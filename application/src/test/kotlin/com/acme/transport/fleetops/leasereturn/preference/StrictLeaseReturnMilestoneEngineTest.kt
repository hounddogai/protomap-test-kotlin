package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictLeaseReturnMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictLeaseReturnMilestoneEngine(newRegistry())
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
