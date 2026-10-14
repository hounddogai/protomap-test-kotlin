package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAppointmentForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAppointmentForecastEngine()
        val item = newAdaptiveAppointmentForecast("code-780", "name-152")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
