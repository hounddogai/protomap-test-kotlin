package com.acme.transport.safety.incident.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedIncidentAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedIncidentAuditEngine()
        val item = newBatchedIncidentAudit("code-262", "title-505")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
