package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAuditTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAuditTemplateEngine()
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
