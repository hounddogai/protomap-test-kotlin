package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingIncidentRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingIncidentRequestEngine(newRegistry())
        val item = newPendingIncidentRequest("code-351", "title-42")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
