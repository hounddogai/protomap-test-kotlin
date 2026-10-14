package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientIncidentAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientIncidentAdjustmentEngine()
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
