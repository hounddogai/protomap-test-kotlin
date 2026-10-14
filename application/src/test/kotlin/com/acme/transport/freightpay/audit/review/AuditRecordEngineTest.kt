package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditRecordEngine()
        val item = newAuditRecord("code-670", "label-664")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
