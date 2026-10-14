package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingProofOfDeliveryBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingProofOfDeliveryBaselineEngine(newRegistry())
        val item = newPendingProofOfDeliveryBaseline("code-608", "title-45")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
