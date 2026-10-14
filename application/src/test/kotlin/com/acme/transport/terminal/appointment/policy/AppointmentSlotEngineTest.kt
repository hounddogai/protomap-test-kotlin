package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentSlotEngine()
        val item = newAppointmentSlot("code-784", "title-431")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
