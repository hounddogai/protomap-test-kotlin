package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDefectSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalDefectSnapshot("code-957", "name-158")
        assertEquals(GlobalDefectSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalDefectSnapshot("code-957", "name-158")
        try {
            item.moveTo(GlobalDefectSnapshotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDefectSnapshotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalDefectSnapshot("code-957", "name-158")
        item.status = GlobalDefectSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalDefectSnapshotStatus.REQUESTED) }
    }
}
