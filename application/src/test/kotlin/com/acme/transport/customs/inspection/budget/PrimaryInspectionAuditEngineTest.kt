package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryInspectionAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryInspectionAuditEngine()
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
