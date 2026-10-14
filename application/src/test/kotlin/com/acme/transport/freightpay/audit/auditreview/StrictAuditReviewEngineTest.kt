package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAuditReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAuditReviewEngine(newRegistry())
        val item = newStrictAuditReview("code-561", "label-212")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
