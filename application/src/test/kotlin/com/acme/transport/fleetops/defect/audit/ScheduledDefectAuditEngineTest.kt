package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectAuditEngine()
        val item = newScheduledDefectAudit("code-581", "label-908")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
