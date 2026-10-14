package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeReservationEngine(newRegistry())
        val item = newDisputeReservation("code-827", "title-541")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
