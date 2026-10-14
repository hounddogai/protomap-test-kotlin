package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyProofOfDeliveryWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyProofOfDeliveryWindowEngine()
        val item = newLegacyProofOfDeliveryWindow("code-446", "title-128")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
