package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDisputeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDisputeBaselineEngine(newRegistry())
        val item = newPendingDisputeBaseline("code-687", "label-916")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
