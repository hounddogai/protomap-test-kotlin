package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionAuditEngine()
        val item = newInspectionAudit("code-85", "title-16")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
