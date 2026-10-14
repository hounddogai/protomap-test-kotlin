package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentExceptionEngine()
        val item = newIncidentException("code-986", "label-16")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
