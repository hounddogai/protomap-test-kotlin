package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newRosterCheckpoint("code-127", "name-664")
        assertEquals(RosterCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRosterCheckpoint("code-127", "name-664")
        try {
            item.moveTo(RosterCheckpointStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterCheckpointStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRosterCheckpoint("code-127", "name-664")
        item.status = RosterCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(RosterCheckpointStatus.DRAFT) }
    }
}
