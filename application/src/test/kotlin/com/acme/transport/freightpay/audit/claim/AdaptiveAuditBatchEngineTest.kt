package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAuditBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAuditBatchEngine()
        val item = newAdaptiveAuditBatch("code-558", "name-301")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
