package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDockDoorScore("code-405", "title-216")
        assertEquals(PendingDockDoorScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPendingDockDoorScore("code-405", "title-216")
        try {
            item.moveTo(PendingDockDoorScoreStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorScoreStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDockDoorScore("code-405", "title-216")
        item.status = PendingDockDoorScoreStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDockDoorScoreStatus.DRAFT) }
    }
}
