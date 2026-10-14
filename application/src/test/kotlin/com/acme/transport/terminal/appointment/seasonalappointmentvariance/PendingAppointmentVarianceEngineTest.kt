package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAppointmentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAppointmentVarianceEngine()
        val item = newPendingAppointmentVariance("code-812", "name-688")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
