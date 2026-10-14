package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnTierEngine(newRegistry())
        val item = newLeaseReturnTier("code-531", "label-133")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
