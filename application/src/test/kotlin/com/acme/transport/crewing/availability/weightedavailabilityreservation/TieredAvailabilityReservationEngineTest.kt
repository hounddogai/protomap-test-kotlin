package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAvailabilityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAvailabilityReservationEngine()
        val item = newTieredAvailabilityReservation("code-213", "title-182")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
