package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackBackhaulReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackBackhaulReservationEngine(newRegistry())
        val item = newFallbackBackhaulReservation("code-652", "title-619")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
