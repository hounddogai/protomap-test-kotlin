package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentPreferenceEngine(newRegistry())
        val item = newAppointmentPreference("code-895", "label-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
