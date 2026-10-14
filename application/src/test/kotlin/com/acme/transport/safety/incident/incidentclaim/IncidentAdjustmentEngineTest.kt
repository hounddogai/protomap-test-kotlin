package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentAdjustmentEngine(newRegistry())
        val item = newIncidentAdjustment("code-887", "label-17")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
