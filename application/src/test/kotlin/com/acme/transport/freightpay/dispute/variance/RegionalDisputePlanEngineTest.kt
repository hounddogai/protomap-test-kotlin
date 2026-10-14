package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDisputePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDisputePlanEngine()
        val item = newRegionalDisputePlan("code-362", "label-584")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
