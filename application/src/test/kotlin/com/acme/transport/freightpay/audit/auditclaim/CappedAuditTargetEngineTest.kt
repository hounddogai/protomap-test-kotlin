package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAuditTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAuditTargetEngine()
        val item = newCappedAuditTarget("code-490", "name-299")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
