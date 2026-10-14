package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRestBreakSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalRestBreakSnapshot("code-917", "title-290")
        assertEquals(GlobalRestBreakSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalRestBreakSnapshot("code-917", "title-290")
        try {
            item.moveTo(GlobalRestBreakSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRestBreakSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalRestBreakSnapshot("code-917", "title-290")
        item.status = GlobalRestBreakSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalRestBreakSnapshotStatus.IDLE) }
    }
}
