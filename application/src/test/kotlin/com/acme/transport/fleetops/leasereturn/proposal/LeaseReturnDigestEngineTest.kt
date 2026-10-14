package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnDigestEngine(newRegistry())
        val item = newLeaseReturnDigest("code-227", "name-688")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
