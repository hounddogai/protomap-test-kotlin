package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAppointmentClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAppointmentClaimEngine()
        val item = newTieredAppointmentClaim("code-332", "title-711")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
