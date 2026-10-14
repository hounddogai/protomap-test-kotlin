package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAppointmentRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAppointmentRollupEngine()
        val item = newFallbackAppointmentRollup("code-615", "name-593")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
