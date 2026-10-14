package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingProofOfDeliveryEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingProofOfDeliveryEntryEngine()
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
