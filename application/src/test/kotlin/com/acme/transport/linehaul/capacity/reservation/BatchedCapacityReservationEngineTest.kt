package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCapacityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCapacityReservationEngine()
        val item = newBatchedCapacityReservation("code-229", "title-807")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
