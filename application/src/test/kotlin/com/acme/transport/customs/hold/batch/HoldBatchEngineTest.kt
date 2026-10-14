package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldBatchEngine()
        val item = newHoldBatch("code-102", "title-672")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
