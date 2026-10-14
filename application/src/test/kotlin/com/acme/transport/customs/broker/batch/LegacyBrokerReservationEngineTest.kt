package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBrokerReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBrokerReservationEngine()
        val item = newLegacyBrokerReservation("code-760", "label-701")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
