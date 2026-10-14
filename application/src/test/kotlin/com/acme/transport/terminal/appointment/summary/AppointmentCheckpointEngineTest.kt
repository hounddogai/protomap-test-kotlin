package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentCheckpointEngine(newRegistry())
        val item = newAppointmentCheckpoint("code-437", "name-261")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
