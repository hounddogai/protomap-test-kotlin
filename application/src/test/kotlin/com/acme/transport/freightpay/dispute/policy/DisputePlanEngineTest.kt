package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputePlanEngine(newRegistry())
        val item = newDisputePlan("code-856", "label-318")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
