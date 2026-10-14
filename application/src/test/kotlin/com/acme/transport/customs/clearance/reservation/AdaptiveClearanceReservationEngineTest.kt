package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveClearanceReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveClearanceReservationEngine(newRegistry())
        val item = newAdaptiveClearanceReservation("code-608", "label-935")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
