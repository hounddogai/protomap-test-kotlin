package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDropSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalDropSnapshot("code-225", "title-901")
        assertEquals(GlobalDropSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalDropSnapshot("code-225", "title-901")
        try {
            item.moveTo(GlobalDropSnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDropSnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalDropSnapshot("code-225", "title-901")
        item.status = GlobalDropSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalDropSnapshotStatus.OPEN) }
    }
}
