package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newAppointmentPolicy("code-336", "name-202")
        assertEquals(AppointmentPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAppointmentPolicy("code-336", "name-202")
        try {
            item.moveTo(AppointmentPolicyStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentPolicyStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAppointmentPolicy("code-336", "name-202")
        item.status = AppointmentPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(AppointmentPolicyStatus.OPEN) }
    }
}
