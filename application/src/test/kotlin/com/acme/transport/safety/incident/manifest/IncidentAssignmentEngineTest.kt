package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentAssignmentEngine(newRegistry())
        val item = newIncidentAssignment("code-402", "title-205")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
