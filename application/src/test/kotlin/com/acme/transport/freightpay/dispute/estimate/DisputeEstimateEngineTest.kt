package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeEstimateEngine(newRegistry())
        val item = newDisputeEstimate("code-892", "label-638")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
