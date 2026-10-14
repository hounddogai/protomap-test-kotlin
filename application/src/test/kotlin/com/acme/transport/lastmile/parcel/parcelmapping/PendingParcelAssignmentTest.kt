package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingParcelAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingParcelAssignment("code-152", "title-526")
        assertEquals(PendingParcelAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingParcelAssignment("code-152", "title-526")
        try {
            item.moveTo(PendingParcelAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingParcelAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingParcelAssignment("code-152", "title-526")
        item.status = PendingParcelAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(PendingParcelAssignmentStatus.IDLE) }
    }
}
