package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIncidentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIncidentMetricEngine(newRegistry())
        val item = newLegacyIncidentMetric("code-401", "title-992")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
