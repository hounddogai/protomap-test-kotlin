package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAppointmentCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAppointmentCycleEngine(newRegistry())
        val item = newPendingAppointmentCycle("code-906", "name-311")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
