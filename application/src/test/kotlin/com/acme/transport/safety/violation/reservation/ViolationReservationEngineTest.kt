package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationReservationEngine(newRegistry())
        val item = newViolationReservation("code-432", "label-966")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
