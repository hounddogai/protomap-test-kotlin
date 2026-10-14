package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIncidentExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIncidentExceptionEngine()
        val item = newManualIncidentException("code-892", "title-993")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
