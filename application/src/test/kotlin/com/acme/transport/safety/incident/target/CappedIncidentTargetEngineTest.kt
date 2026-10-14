package com.acme.transport.safety.incident.target

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedIncidentTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedIncidentTargetEngine()
        val item = newCappedIncidentTarget("code-349", "title-485")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
