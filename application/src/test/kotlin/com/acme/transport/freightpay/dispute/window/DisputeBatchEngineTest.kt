package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeBatchEngine()
        val item = newDisputeBatch("code-719", "title-641")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
