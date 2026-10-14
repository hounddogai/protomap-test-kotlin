package com.acme.transport.safety.incident.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentWindowEngine(newRegistry())
        val item = newIncidentWindow("code-207", "label-302")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
