package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDisputeRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDisputeRuleEngine(newRegistry())
        val item = newRegionalDisputeRule("code-25", "label-439")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
