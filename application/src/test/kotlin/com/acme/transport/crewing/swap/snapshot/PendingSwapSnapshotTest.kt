package com.acme.transport.crewing.swap.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSwapSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newPendingSwapSnapshot("code-924", "title-198")
        assertEquals(PendingSwapSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingSwapSnapshot("code-924", "title-198")
        try {
            item.moveTo(PendingSwapSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSwapSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingSwapSnapshot("code-924", "title-198")
        item.status = PendingSwapSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(PendingSwapSnapshotStatus.CREATED) }
    }
}
