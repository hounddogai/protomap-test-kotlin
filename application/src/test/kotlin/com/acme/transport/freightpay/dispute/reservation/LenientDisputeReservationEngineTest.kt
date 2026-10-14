package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDisputeReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDisputeReservationEngine(newRegistry())
        val item = newLenientDisputeReservation("code-569", "name-366")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
