package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentDraftEngine(newRegistry())
        val item = newAppointmentDraft("code-10", "name-106")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
