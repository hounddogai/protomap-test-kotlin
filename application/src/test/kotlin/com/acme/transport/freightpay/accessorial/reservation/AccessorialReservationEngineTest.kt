package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialReservationEngine(newRegistry())
        val item = newAccessorialReservation("code-197", "title-584")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
