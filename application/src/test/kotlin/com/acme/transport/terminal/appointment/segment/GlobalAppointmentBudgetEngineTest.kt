package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAppointmentBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAppointmentBudgetEngine(newRegistry())
        val item = newGlobalAppointmentBudget("code-440", "label-553")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
