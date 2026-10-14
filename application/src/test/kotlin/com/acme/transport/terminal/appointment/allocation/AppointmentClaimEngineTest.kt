package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentClaimEngine(newRegistry())
        val item = newAppointmentClaim("code-648", "title-210")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
