package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditTemplateEngine(newRegistry())
        val item = newAuditTemplate("code-647", "label-734")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
