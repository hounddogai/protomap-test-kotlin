package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentTierEngine()
        val item = newIncidentTier("code-285", "label-178")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
