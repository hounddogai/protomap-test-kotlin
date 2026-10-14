package com.acme.transport.safety.incident.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentBatchEngine(newRegistry())
        val item = newIncidentBatch("code-358", "label-894")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
