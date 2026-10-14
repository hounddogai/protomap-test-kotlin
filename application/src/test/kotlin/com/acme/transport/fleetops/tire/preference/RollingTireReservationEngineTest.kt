package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTireReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTireReservationEngine(newRegistry())
        val item = newRollingTireReservation("code-726", "title-950")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
