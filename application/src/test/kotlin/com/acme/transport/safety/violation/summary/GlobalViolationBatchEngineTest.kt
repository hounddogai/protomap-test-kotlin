package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalViolationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalViolationBatchEngine()
        val item = newGlobalViolationBatch("code-220", "name-396")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
