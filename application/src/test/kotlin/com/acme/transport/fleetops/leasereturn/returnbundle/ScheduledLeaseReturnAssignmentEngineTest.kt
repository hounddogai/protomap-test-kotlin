package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLeaseReturnAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLeaseReturnAssignmentEngine(newRegistry())
        val item = newScheduledLeaseReturnAssignment("code-865", "name-436")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
