package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentSnapshotEngine(newRegistry())
        val item = newAppointmentSnapshot("code-128", "label-874")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
