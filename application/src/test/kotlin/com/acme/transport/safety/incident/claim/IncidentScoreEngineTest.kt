package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentScoreEngine()
        val item = newIncidentScore("code-897", "title-580")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
