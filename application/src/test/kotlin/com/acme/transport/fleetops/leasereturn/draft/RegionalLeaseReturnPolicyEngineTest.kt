package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalLeaseReturnPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalLeaseReturnPolicyEngine(newRegistry())
        val item = newRegionalLeaseReturnPolicy("code-61", "label-510")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
