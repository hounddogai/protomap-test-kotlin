package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentTargetEngine(newRegistry())
        val item = newAppointmentTarget("code-228", "name-10")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
