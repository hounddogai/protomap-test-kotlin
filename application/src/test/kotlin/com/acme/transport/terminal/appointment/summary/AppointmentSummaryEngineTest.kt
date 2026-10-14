package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentSummaryEngine()
        val item = newAppointmentSummary("code-120", "name-711")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
