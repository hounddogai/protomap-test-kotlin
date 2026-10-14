package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAppointmentForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveAppointmentForecast("code-780", "name-152")
        assertEquals(AdaptiveAppointmentForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveAppointmentForecast("code-780", "name-152")
        try {
            item.moveTo(AdaptiveAppointmentForecastStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAppointmentForecastStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveAppointmentForecast("code-780", "name-152")
        item.status = AdaptiveAppointmentForecastStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveAppointmentForecastStatus.DRAFT) }
    }
}
