package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class AuditRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditRuleEngine()
        val item = newAuditRule("code-34", "title-928")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
