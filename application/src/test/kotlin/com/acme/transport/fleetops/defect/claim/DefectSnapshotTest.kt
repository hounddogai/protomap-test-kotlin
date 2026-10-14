package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectSnapshot("code-364", "name-752")
        assertEquals(DefectSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefectSnapshot("code-364", "name-752")
        try {
            item.moveTo(DefectSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectSnapshot("code-364", "name-752")
        item.status = DefectSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(DefectSnapshotStatus.IDLE) }
    }
}
