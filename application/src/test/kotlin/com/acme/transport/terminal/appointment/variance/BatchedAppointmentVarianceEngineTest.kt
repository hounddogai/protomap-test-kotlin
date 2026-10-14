package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAppointmentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAppointmentVarianceEngine()
        val item = newBatchedAppointmentVariance("code-781", "label-952")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
