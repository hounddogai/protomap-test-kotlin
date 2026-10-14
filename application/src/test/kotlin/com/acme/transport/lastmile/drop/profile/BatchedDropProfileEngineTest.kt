package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDropProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDropProfileEngine()
        val item = newBatchedDropProfile("code-385", "label-442")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
