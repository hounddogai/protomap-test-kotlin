package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDisputePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDisputePolicyEngine(newRegistry())
        val item = newCappedDisputePolicy("code-834", "title-45")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
