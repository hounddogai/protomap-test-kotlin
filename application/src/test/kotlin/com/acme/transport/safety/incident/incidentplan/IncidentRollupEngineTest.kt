package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentRollupEngine()
        val item = newIncidentRollup("code-470", "label-896")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
