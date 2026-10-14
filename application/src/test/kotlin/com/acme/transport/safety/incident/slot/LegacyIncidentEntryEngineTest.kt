package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIncidentEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIncidentEntryEngine()
        val item = newLegacyIncidentEntry("code-850", "label-51")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
