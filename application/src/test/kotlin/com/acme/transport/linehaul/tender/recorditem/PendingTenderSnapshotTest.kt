package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTenderSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingTenderSnapshot("code-260", "label-375")
        assertEquals(PendingTenderSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingTenderSnapshot("code-260", "label-375")
        try {
            item.moveTo(PendingTenderSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTenderSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingTenderSnapshot("code-260", "label-375")
        item.status = PendingTenderSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingTenderSnapshotStatus.PROPOSED) }
    }
}
