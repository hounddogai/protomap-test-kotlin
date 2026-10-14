package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAppointmentMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAppointmentMappingEngine(newRegistry())
        val item = newStrictAppointmentMapping("code-43", "title-963")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
