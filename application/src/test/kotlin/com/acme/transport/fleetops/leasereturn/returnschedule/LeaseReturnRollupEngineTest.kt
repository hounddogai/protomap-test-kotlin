package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnRollupEngine(newRegistry())
        val item = newLeaseReturnRollup("code-591", "name-222")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
