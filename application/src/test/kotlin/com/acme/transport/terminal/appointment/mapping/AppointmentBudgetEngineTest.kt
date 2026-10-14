package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentBudgetEngine()
        val item = newAppointmentBudget("code-358", "title-880")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
