package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAppointmentOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAppointmentOverrideEngine()
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
