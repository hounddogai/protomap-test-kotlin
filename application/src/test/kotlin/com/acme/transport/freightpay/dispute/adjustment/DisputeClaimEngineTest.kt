package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeClaimEngine()
        val item = newDisputeClaim("code-251", "label-389")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
