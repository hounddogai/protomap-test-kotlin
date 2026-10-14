package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedDockDoorTier("code-803", "title-568")
        assertEquals(CappedDockDoorTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedDockDoorTier("code-803", "title-568")
        try {
            item.moveTo(CappedDockDoorTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedDockDoorTier("code-803", "title-568")
        item.status = CappedDockDoorTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedDockDoorTierStatus.PROPOSED) }
    }
}
