package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDropEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryDropEntry("code-574", "title-713")
        assertEquals(PrimaryDropEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryDropEntry("code-574", "title-713")
        try {
            item.moveTo(PrimaryDropEntryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDropEntryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryDropEntry("code-574", "title-713")
        item.status = PrimaryDropEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryDropEntryStatus.PROPOSED) }
    }
}
