package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAppointmentScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAppointmentScoreEngine(newRegistry())
        val item = newBatchedAppointmentScore("code-559", "title-107")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
