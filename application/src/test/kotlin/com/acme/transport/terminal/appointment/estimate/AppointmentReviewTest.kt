package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newAppointmentReview("code-567", "title-933")
        assertEquals(AppointmentReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAppointmentReview("code-567", "title-933")
        try {
            item.moveTo(AppointmentReviewStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentReviewStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAppointmentReview("code-567", "title-933")
        item.status = AppointmentReviewStatus.CLOSED
        assertFails("move") { item.moveTo(AppointmentReviewStatus.DRAFT) }
    }
}
