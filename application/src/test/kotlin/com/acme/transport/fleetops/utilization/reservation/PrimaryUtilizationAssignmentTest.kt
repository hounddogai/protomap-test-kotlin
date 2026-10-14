package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryUtilizationAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryUtilizationAssignment("code-821", "title-968")
        assertEquals(PrimaryUtilizationAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryUtilizationAssignment("code-821", "title-968")
        try {
            item.moveTo(PrimaryUtilizationAssignmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryUtilizationAssignmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryUtilizationAssignment("code-821", "title-968")
        item.status = PrimaryUtilizationAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryUtilizationAssignmentStatus.NEW) }
    }
}
