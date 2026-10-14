package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newAppointmentThreshold("code-19", "title-381")
        assertEquals(AppointmentThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAppointmentThreshold("code-19", "title-381")
        try {
            item.moveTo(AppointmentThresholdStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentThresholdStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAppointmentThreshold("code-19", "title-381")
        item.status = AppointmentThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(AppointmentThresholdStatus.OPEN) }
    }
}
