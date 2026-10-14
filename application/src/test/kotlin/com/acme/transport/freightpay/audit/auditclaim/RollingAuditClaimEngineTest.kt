package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAuditClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAuditClaimEngine()
        val item = newRollingAuditClaim("code-918", "name-259")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
