package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAppointmentCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAppointmentCycleEngine(newRegistry())
        val item = newAdaptiveAppointmentCycle("code-235", "label-454")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
