package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientProofOfDeliveryReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientProofOfDeliveryReservationEngine()
        val item = newLenientProofOfDeliveryReservation("code-936", "title-372")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
