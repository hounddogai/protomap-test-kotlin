package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAppointmentForecastTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledAppointmentForecast("code-716", "title-776")
        assertEquals(ScheduledAppointmentForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledAppointmentForecast("code-716", "title-776")
        try {
            item.moveTo(ScheduledAppointmentForecastStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAppointmentForecastStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledAppointmentForecast("code-716", "title-776")
        item.status = ScheduledAppointmentForecastStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledAppointmentForecastStatus.NEW) }
    }
}
