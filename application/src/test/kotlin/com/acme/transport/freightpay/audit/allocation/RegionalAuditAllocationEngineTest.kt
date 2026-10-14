package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAuditAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAuditAllocationEngine(newRegistry())
        val item = newRegionalAuditAllocation("code-620", "title-272")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
