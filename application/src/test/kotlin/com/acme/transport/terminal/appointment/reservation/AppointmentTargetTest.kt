package com.acme.transport.terminal.appointment.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentTargetTest {
    @Test
    fun `starts in open`() {
        val item = newAppointmentTarget("code-228", "name-10")
        assertEquals(AppointmentTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAppointmentTarget("code-228", "name-10")
        try {
            item.moveTo(AppointmentTargetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentTargetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAppointmentTarget("code-228", "name-10")
        item.status = AppointmentTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(AppointmentTargetStatus.OPEN) }
    }
}
