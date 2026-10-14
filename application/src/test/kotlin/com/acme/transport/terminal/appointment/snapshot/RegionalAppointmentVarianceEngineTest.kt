package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAppointmentVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAppointmentVarianceEngine()
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
