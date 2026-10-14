package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newAppointmentSegment("code-568", "title-431")
        assertEquals(AppointmentSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAppointmentSegment("code-568", "title-431")
        try {
            item.moveTo(AppointmentSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAppointmentSegment("code-568", "title-431")
        item.status = AppointmentSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(AppointmentSegmentStatus.PLANNED) }
    }
}
