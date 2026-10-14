package com.acme.transport.terminal.sortation.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveSortationSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        assertEquals(AdaptiveSortationSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        try {
            item.moveTo(AdaptiveSortationSnapshotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveSortationSnapshotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        item.status = AdaptiveSortationSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveSortationSnapshotStatus.NEW) }
    }
}
