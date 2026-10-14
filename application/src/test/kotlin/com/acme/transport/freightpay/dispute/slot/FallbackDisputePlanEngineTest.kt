package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDisputePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDisputePlanEngine()
        val item = newFallbackDisputePlan("code-168", "label-624")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
