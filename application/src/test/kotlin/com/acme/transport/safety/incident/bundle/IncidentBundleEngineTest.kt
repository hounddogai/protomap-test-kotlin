package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentBundleEngine()
        val item = newIncidentBundle("code-7", "name-362")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
