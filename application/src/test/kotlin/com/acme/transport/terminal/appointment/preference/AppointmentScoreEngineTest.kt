package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentScoreEngine()
        val item = newAppointmentScore("code-306", "name-830")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
