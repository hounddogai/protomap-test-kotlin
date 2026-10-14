package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAppointmentBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAppointmentBundleEngine(newRegistry())
        val item = newRollingAppointmentBundle("code-622", "label-590")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
