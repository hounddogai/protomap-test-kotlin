package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDefectSnapshot("code-954", "label-203")
        assertEquals(ScheduledDefectSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledDefectSnapshot("code-954", "label-203")
        try {
            item.moveTo(ScheduledDefectSnapshotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectSnapshotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDefectSnapshot("code-954", "label-203")
        item.status = ScheduledDefectSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDefectSnapshotStatus.DRAFT) }
    }
}
