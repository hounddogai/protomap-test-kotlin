package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentDraftEngine()
        val item = newIncidentDraft("code-351", "label-874")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
