package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAppointmentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAppointmentVarianceEngine(newRegistry())
        val item = newStrictAppointmentVariance("code-886", "name-301")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
