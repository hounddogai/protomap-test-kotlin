package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLeaseReturnRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLeaseReturnRequestEngine(newRegistry())
        val item = newLegacyLeaseReturnRequest("code-775", "title-417")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
