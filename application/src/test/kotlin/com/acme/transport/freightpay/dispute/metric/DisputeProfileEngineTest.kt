package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeProfileEngine()
        val item = newDisputeProfile("code-679", "title-644")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
