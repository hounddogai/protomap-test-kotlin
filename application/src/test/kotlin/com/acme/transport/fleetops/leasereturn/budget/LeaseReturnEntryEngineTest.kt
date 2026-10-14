package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnEntryEngine(newRegistry())
        val item = newLeaseReturnEntry("code-751", "title-707")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
