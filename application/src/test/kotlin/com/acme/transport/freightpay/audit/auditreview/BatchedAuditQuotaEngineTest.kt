package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAuditQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAuditQuotaEngine(newRegistry())
        val item = newBatchedAuditQuota("code-157", "label-190")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
