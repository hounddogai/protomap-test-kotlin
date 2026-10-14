package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAppointmentNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAppointmentNoticeEngine(newRegistry())
        val item = newBatchedAppointmentNotice("code-570", "label-193")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
