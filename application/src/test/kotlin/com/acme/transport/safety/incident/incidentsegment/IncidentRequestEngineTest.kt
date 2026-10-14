package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentRequestEngine(newRegistry())
        val item = newIncidentRequest("code-654", "title-81")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
