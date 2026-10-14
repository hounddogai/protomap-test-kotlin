package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyProofOfDeliveryRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyProofOfDeliveryRequestEngine(newRegistry())
        val item = newLegacyProofOfDeliveryRequest("code-489", "title-869")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
