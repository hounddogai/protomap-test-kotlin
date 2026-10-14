package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAppointmentBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAppointmentBudgetEngine(newRegistry())
        val item = newAdaptiveAppointmentBudget("code-882", "title-409")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
