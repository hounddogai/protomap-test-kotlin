package com.acme.transport.terminal.appointment.window

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAppointmentWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAppointmentWindowEngine()
        val item = newManualAppointmentWindow("code-374", "label-362")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
