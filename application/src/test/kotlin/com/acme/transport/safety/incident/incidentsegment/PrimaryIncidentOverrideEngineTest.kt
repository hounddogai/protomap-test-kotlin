package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryIncidentOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryIncidentOverrideEngine(newRegistry())
        val item = newPrimaryIncidentOverride("code-820", "title-318")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
