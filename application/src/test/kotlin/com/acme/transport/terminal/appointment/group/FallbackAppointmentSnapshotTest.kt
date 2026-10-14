package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackAppointmentSnapshot("code-486", "label-93")
        assertEquals(FallbackAppointmentSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackAppointmentSnapshot("code-486", "label-93")
        try {
            item.moveTo(FallbackAppointmentSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackAppointmentSnapshot("code-486", "label-93")
        item.status = FallbackAppointmentSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(FallbackAppointmentSnapshotStatus.NEW) }
    }
}
