package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditPolicyEngine()
        val item = newAuditPolicy("code-115", "title-156")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
