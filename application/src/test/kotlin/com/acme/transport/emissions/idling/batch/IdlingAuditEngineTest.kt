package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingAuditEngine()
        val item = newIdlingAudit("code-505", "label-837")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
