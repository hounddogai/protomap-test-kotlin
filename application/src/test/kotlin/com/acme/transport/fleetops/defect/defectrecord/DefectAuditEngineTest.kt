package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectAuditEngine()
        val item = newDefectAudit("code-703", "name-861")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
