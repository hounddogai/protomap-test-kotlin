package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredSortationCheckpoint("code-497", "name-980")
        assertEquals(TieredSortationCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredSortationCheckpoint("code-497", "name-980")
        try {
            item.moveTo(TieredSortationCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredSortationCheckpoint("code-497", "name-980")
        item.status = TieredSortationCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredSortationCheckpointStatus.REQUESTED) }
    }
}
