package com.acme.transport.safety.incident.defaultincidentplan

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentMappingEngine()
        val item = newIncidentMapping("code-452", "label-347")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
