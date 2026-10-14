package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredIncidentBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredIncidentBaselineEngine(newRegistry())
        val item = newTieredIncidentBaseline("code-763", "name-806")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
