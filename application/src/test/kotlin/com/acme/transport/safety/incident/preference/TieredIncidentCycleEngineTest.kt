package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredIncidentCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredIncidentCycleEngine()
        val item = newTieredIncidentCycle("code-766", "name-832")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
