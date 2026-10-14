package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        assertEquals(RegionalDefectSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        try {
            item.moveTo(RegionalDefectSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        item.status = RegionalDefectSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalDefectSnapshotStatus.IDLE) }
    }
}
