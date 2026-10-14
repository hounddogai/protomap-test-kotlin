package com.acme.transport.terminal.sortation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newSortationCheckpoint("code-229", "name-709")
        assertEquals(SortationCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newSortationCheckpoint("code-229", "name-709")
        try {
            item.moveTo(SortationCheckpointStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationCheckpointStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSortationCheckpoint("code-229", "name-709")
        item.status = SortationCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(SortationCheckpointStatus.NEW) }
    }
}
