package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newAppointmentSummary("code-120", "name-711")
        assertEquals(AppointmentSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAppointmentSummary("code-120", "name-711")
        try {
            item.moveTo(AppointmentSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAppointmentSummary("code-120", "name-711")
        item.status = AppointmentSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AppointmentSummaryStatus.PROPOSED) }
    }
}
