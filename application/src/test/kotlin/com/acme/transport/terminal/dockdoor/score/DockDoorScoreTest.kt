package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorScore("code-795", "name-104")
        assertEquals(DockDoorScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDockDoorScore("code-795", "name-104")
        try {
            item.moveTo(DockDoorScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorScore("code-795", "name-104")
        item.status = DockDoorScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorScoreStatus.PROPOSED) }
    }
}
