package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAuditSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAuditSegmentEngine()
        val item = newWeightedAuditSegment("code-246", "title-581")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
