package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTireCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingTireCheckpoint("code-446", "name-852")
        assertEquals(RollingTireCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingTireCheckpoint("code-446", "name-852")
        try {
            item.moveTo(RollingTireCheckpointStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTireCheckpointStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingTireCheckpoint("code-446", "name-852")
        item.status = RollingTireCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(RollingTireCheckpointStatus.DRAFT) }
    }
}
