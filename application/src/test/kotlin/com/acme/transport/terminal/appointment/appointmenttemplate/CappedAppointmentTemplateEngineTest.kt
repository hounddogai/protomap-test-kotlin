package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAppointmentTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAppointmentTemplateEngine(newRegistry())
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
