package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingInspectionCheckpoint("code-363", "title-710")
        assertEquals(PendingInspectionCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingInspectionCheckpoint("code-363", "title-710")
        try {
            item.moveTo(PendingInspectionCheckpointStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionCheckpointStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingInspectionCheckpoint("code-363", "title-710")
        item.status = PendingInspectionCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingInspectionCheckpointStatus.PLANNED) }
    }
}
