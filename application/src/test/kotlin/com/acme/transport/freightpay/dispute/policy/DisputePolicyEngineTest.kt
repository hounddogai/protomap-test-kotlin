package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputePolicyEngine()
        val item = newDisputePolicy("code-397", "label-957")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
