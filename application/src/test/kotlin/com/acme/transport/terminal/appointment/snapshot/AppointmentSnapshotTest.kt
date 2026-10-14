package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AppointmentSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newAppointmentSnapshot("code-128", "label-874")
        assertEquals(AppointmentSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAppointmentSnapshot("code-128", "label-874")
        try {
            item.moveTo(AppointmentSnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AppointmentSnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAppointmentSnapshot("code-128", "label-874")
        item.status = AppointmentSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(AppointmentSnapshotStatus.IDLE) }
    }
}
