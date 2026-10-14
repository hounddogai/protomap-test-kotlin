package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class DropBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropBatchEngine()
        val item = newDropBatch("code-143", "title-771")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
