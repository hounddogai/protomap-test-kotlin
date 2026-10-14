package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentNoticeEngine()
        val item = newAppointmentNotice("code-370", "name-41")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
