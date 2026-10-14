package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationSnapshot("code-806", "name-829")
        assertEquals(SortationSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSortationSnapshot("code-806", "name-829")
        try {
            item.moveTo(SortationSnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationSnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationSnapshot("code-806", "name-829")
        item.status = SortationSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationSnapshotStatus.REQUESTED) }
    }
}
