package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalIncidentRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalIncidentRequestEngine(newRegistry())
        val item = newGlobalIncidentRequest("code-318", "title-15")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
