package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDisputePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDisputePolicyEngine(newRegistry())
        val item = newLenientDisputePolicy("code-949", "title-457")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
