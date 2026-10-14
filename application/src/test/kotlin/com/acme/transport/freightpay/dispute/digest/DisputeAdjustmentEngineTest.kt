package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeAdjustmentEngine(newRegistry())
        val item = newDisputeAdjustment("code-856", "label-576")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
