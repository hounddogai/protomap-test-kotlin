package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSortationTierTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledSortationTier("code-765", "name-444")
        assertEquals(ScheduledSortationTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledSortationTier("code-765", "name-444")
        try {
            item.moveTo(ScheduledSortationTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSortationTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledSortationTier("code-765", "name-444")
        item.status = ScheduledSortationTierStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledSortationTierStatus.DRAFT) }
    }
}
