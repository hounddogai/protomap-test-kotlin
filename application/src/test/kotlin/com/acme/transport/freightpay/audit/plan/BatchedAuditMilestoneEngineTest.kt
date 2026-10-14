package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAuditMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAuditMilestoneEngine()
        val item = newBatchedAuditMilestone("code-622", "title-343")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
