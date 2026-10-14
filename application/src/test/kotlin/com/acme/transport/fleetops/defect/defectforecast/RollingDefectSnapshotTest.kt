package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDefectSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingDefectSnapshot("code-831", "label-291")
        assertEquals(RollingDefectSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingDefectSnapshot("code-831", "label-291")
        try {
            item.moveTo(RollingDefectSnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDefectSnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingDefectSnapshot("code-831", "label-291")
        item.status = RollingDefectSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(RollingDefectSnapshotStatus.IDLE) }
    }
}
