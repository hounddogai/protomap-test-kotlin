package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditSegmentEngine()
        val item = newAuditSegment("code-87", "title-857")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
