package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSortationSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingSortationSegment("code-402", "name-193")
        assertEquals(PendingSortationSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingSortationSegment("code-402", "name-193")
        try {
            item.moveTo(PendingSortationSegmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSortationSegmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingSortationSegment("code-402", "name-193")
        item.status = PendingSortationSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingSortationSegmentStatus.REQUESTED) }
    }
}
