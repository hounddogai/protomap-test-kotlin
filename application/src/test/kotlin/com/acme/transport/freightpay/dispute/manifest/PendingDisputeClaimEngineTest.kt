package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDisputeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDisputeClaimEngine(newRegistry())
        val item = newPendingDisputeClaim("code-413", "name-765")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
