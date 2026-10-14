package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentLedgerEngine(newRegistry())
        val item = newAppointmentLedger("code-773", "name-957")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
