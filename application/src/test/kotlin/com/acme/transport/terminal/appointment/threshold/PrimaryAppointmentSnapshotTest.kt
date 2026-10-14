package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAppointmentSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryAppointmentSnapshot("code-625", "label-414")
        assertEquals(PrimaryAppointmentSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryAppointmentSnapshot("code-625", "label-414")
        try {
            item.moveTo(PrimaryAppointmentSnapshotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAppointmentSnapshotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryAppointmentSnapshot("code-625", "label-414")
        item.status = PrimaryAppointmentSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryAppointmentSnapshotStatus.CREATED) }
    }
}
