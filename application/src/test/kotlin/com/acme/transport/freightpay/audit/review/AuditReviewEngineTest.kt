package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditReviewEngine(newRegistry())
        val item = newAuditReview("code-170", "name-989")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
