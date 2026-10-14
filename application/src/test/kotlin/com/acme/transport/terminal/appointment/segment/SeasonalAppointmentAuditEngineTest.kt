package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAppointmentAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAppointmentAuditEngine(newRegistry())
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
