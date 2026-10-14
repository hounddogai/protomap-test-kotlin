package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentManifestEngine()
        val item = newAppointmentManifest("code-558", "name-381")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
