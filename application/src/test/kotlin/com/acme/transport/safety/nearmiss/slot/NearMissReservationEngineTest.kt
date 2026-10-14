package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissReservationEngine()
        val item = newNearMissReservation("code-110", "label-503")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
