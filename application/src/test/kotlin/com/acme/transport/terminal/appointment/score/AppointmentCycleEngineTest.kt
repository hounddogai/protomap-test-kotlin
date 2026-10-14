package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentCycleEngine(newRegistry())
        val item = newAppointmentCycle("code-526", "name-400")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
