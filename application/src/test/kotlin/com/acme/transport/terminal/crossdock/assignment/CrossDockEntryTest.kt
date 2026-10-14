package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newCrossDockEntry("code-347", "title-160")
        assertEquals(CrossDockEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCrossDockEntry("code-347", "title-160")
        try {
            item.moveTo(CrossDockEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCrossDockEntry("code-347", "title-160")
        item.status = CrossDockEntryStatus.CLOSED
        assertFails("move") { item.moveTo(CrossDockEntryStatus.DRAFT) }
    }
}
