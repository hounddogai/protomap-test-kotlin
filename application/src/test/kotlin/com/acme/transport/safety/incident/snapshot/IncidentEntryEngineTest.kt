package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentEntryEngine()
        val item = newIncidentEntry("code-193", "label-941")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
