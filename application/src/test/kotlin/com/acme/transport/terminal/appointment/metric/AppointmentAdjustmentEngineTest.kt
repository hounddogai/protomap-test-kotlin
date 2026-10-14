package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentAdjustmentEngine()
        val item = newAppointmentAdjustment("code-971", "label-105")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
