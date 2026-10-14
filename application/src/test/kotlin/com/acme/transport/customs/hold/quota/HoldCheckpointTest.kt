package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newHoldCheckpoint("code-720", "name-446")
        assertEquals(HoldCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHoldCheckpoint("code-720", "name-446")
        try {
            item.moveTo(HoldCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHoldCheckpoint("code-720", "name-446")
        item.status = HoldCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HoldCheckpointStatus.PROPOSED) }
    }
}
