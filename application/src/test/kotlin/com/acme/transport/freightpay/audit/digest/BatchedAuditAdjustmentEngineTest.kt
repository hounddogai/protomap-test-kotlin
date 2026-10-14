package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAuditAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAuditAdjustmentEngine()
        val item = newBatchedAuditAdjustment("code-628", "title-985")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
