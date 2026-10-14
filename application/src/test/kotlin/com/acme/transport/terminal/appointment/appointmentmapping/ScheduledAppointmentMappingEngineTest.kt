package com.acme.transport.terminal.appointment.appointmentmapping

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAppointmentMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAppointmentMappingEngine()
        val item = newScheduledAppointmentMapping("code-270", "title-999")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
