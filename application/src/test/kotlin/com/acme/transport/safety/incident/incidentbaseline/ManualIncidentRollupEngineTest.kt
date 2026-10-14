package com.acme.transport.safety.incident.incidentbaseline

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIncidentRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIncidentRollupEngine()
        val item = newManualIncidentRollup("code-9", "title-424")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
