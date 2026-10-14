package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationReservationEngine()
        val item = newUtilizationReservation("code-145", "title-546")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
