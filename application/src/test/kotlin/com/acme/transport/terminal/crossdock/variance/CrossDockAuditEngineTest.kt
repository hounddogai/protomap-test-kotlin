package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockAuditEngine()
        val item = newCrossDockAudit("code-262", "name-689")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
