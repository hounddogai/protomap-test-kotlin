package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayReservationEngine(newRegistry())
        val item = newRelayReservation("code-327", "name-768")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
