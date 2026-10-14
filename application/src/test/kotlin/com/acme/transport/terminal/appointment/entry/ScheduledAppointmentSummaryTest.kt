package com.acme.transport.terminal.appointment.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAppointmentSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledAppointmentSummary("code-797", "label-881")
        assertEquals(ScheduledAppointmentSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledAppointmentSummary("code-797", "label-881")
        try {
            item.moveTo(ScheduledAppointmentSummaryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAppointmentSummaryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledAppointmentSummary("code-797", "label-881")
        item.status = ScheduledAppointmentSummaryStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledAppointmentSummaryStatus.NEW) }
    }
}
