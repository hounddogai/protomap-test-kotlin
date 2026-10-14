package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAppointmentSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingAppointmentSnapshot("code-913", "label-207")
        assertEquals(RollingAppointmentSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingAppointmentSnapshot("code-913", "label-207")
        try {
            item.moveTo(RollingAppointmentSnapshotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAppointmentSnapshotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingAppointmentSnapshot("code-913", "label-207")
        item.status = RollingAppointmentSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingAppointmentSnapshotStatus.PLANNED) }
    }
}
