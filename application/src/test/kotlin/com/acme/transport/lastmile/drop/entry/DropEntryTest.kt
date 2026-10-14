package com.acme.transport.lastmile.drop.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newDropEntry("code-905", "title-991")
        assertEquals(DropEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDropEntry("code-905", "title-991")
        try {
            item.moveTo(DropEntryStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropEntryStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDropEntry("code-905", "title-991")
        item.status = DropEntryStatus.CLOSED
        assertFails("move") { item.moveTo(DropEntryStatus.DRAFT) }
    }
}
