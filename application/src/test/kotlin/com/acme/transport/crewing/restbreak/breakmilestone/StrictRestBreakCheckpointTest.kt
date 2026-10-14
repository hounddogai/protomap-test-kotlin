package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRestBreakCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictRestBreakCheckpoint("code-852", "name-458")
        assertEquals(StrictRestBreakCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newStrictRestBreakCheckpoint("code-852", "name-458")
        try {
            item.moveTo(StrictRestBreakCheckpointStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRestBreakCheckpointStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictRestBreakCheckpoint("code-852", "name-458")
        item.status = StrictRestBreakCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(StrictRestBreakCheckpointStatus.DRAFT) }
    }
}
