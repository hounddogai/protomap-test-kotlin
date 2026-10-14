package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLeaseReturnGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLeaseReturnGroupEngine(newRegistry())
        val item = newRollingLeaseReturnGroup("code-937", "name-768")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
