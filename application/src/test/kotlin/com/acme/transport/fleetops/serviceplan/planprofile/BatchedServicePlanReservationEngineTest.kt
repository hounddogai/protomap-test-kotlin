package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedServicePlanReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedServicePlanReservationEngine()
        val item = newBatchedServicePlanReservation("code-216", "title-356")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
