package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAppointmentBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAppointmentBudgetEngine(newRegistry())
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
