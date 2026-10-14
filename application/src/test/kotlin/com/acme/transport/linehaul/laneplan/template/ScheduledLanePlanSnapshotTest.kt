package com.acme.transport.linehaul.laneplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledLanePlanSnapshot("code-265", "label-397")
        assertEquals(ScheduledLanePlanSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledLanePlanSnapshot("code-265", "label-397")
        try {
            item.moveTo(ScheduledLanePlanSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledLanePlanSnapshot("code-265", "label-397")
        item.status = ScheduledLanePlanSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledLanePlanSnapshotStatus.NEW) }
    }
}
