package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentVarianceEngine()
        val item = newIncidentVariance("code-901", "name-482")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
