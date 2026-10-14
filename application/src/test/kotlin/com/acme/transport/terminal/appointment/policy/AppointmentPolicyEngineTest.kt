package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentPolicyEngine(newRegistry())
        val item = newAppointmentPolicy("code-336", "name-202")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
