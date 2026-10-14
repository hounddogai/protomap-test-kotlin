package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditTierEngine()
        val item = newAuditTier("code-558", "label-398")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
