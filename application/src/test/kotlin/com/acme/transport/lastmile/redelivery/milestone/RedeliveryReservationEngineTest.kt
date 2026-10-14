package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryReservationEngine(newRegistry())
        val item = newRedeliveryReservation("code-366", "name-894")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
