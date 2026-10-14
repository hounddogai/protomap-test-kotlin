package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentOverrideEngine(newRegistry())
        val item = newAppointmentOverride("code-45", "name-651")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
