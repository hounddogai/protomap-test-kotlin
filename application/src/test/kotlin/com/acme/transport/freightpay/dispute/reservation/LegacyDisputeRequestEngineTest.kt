package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDisputeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDisputeRequestEngine()
        val item = newLegacyDisputeRequest("code-891", "label-548")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
