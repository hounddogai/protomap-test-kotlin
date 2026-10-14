package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalIncidentBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalIncidentBatchEngine()
        val item = newGlobalIncidentBatch("code-151", "title-561")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
