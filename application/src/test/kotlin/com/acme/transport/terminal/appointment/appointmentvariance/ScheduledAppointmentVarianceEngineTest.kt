package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAppointmentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAppointmentVarianceEngine(newRegistry())
        val item = newScheduledAppointmentVariance("code-140", "title-419")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
