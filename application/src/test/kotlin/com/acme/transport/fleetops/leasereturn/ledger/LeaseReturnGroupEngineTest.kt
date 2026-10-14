package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnGroupEngine(newRegistry())
        val item = newLeaseReturnGroup("code-625", "label-336")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
