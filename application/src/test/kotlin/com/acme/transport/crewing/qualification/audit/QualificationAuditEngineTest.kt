package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationAuditEngine()
        val item = newQualificationAudit("code-959", "label-356")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
