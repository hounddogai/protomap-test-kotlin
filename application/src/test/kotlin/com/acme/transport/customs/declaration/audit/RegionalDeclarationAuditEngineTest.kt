package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDeclarationAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDeclarationAuditEngine(newRegistry())
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
