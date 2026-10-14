package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanReservationEngine()
        val item = newServicePlanReservation("code-794", "name-655")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
