package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAppointmentMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAppointmentMilestoneEngine()
        val item = newManualAppointmentMilestone("code-671", "title-549")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
