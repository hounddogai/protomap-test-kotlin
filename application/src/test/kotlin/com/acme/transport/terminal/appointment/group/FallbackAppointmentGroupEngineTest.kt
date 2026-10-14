package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAppointmentGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAppointmentGroupEngine(newRegistry())
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
