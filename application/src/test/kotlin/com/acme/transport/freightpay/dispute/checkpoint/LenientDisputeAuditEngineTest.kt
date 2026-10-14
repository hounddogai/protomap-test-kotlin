package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDisputeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDisputeAuditEngine()
        val item = newLenientDisputeAudit("code-568", "title-609")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
