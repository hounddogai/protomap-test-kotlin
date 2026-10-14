package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalLeaseReturnAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalLeaseReturnAssignmentEngine(newRegistry())
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
