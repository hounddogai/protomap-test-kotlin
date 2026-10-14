package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentEstimateEngine()
        val item = newAppointmentEstimate("code-121", "label-519")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
