package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDisputeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDisputeDigestEngine(newRegistry())
        val item = newPendingDisputeDigest("code-8", "title-503")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
