package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedDockDoorBaseline("code-206", "title-515")
        assertEquals(CappedDockDoorBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedDockDoorBaseline("code-206", "title-515")
        try {
            item.moveTo(CappedDockDoorBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedDockDoorBaseline("code-206", "title-515")
        item.status = CappedDockDoorBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedDockDoorBaselineStatus.PLANNED) }
    }
}
