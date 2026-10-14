package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newRosterMilestone("code-640", "title-383")
        assertEquals(RosterMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRosterMilestone("code-640", "title-383")
        try {
            item.moveTo(RosterMilestoneStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterMilestoneStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRosterMilestone("code-640", "title-383")
        item.status = RosterMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(RosterMilestoneStatus.OPEN) }
    }
}
