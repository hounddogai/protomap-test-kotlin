package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIncidentPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIncidentPreferenceEngine(newRegistry())
        val item = newLegacyIncidentPreference("code-163", "label-14")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
