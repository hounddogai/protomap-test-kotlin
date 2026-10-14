package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentAllocationEngine(newRegistry())
        val item = newAppointmentAllocation("code-208", "title-740")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
