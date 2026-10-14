package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeAuditEngine()
        val item = newDisputeAudit("code-460", "name-397")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
