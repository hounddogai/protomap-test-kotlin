package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredIncidentPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredIncidentPreferenceEngine()
        val item = newTieredIncidentPreference("code-164", "label-497")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
