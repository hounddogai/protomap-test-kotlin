package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictIncidentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictIncidentMetricEngine(newRegistry())
        val item = newStrictIncidentMetric("code-776", "label-102")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
