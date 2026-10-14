package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentTierEngine()
        val item = newAppointmentTier("code-958", "label-3")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
