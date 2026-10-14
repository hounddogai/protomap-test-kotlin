package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnProfileEngine(newRegistry())
        val item = newLeaseReturnProfile("code-713", "label-714")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
