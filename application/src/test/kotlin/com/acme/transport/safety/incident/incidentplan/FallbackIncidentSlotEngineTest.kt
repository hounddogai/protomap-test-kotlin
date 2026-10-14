package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackIncidentSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackIncidentSlotEngine()
        val item = newFallbackIncidentSlot("code-848", "title-404")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
