package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSwapCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictSwapCheckpoint("code-623", "title-530")
        assertEquals(StrictSwapCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictSwapCheckpoint("code-623", "title-530")
        try {
            item.moveTo(StrictSwapCheckpointStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSwapCheckpointStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictSwapCheckpoint("code-623", "title-530")
        item.status = StrictSwapCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictSwapCheckpointStatus.PLANNED) }
    }
}
