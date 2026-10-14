package com.acme.transport.customs.clearance.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceBatchEngine()
        val item = newClearanceBatch("code-265", "name-933")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
