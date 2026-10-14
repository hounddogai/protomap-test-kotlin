package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentMappingEngine()
        val item = newAppointmentMapping("code-525", "title-647")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
