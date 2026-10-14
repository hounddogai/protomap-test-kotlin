package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAppointmentDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAppointmentDigestEngine(newRegistry())
        val item = newSeasonalAppointmentDigest("code-492", "name-297")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
