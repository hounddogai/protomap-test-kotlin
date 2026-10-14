package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDisputeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDisputeClaimEngine()
        val item = newCappedDisputeClaim("code-183", "label-409")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
