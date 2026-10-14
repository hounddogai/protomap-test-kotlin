package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDropMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalDropMilestone("code-834", "label-292")
        assertEquals(SeasonalDropMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalDropMilestone("code-834", "label-292")
        try {
            item.moveTo(SeasonalDropMilestoneStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDropMilestoneStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalDropMilestone("code-834", "label-292")
        item.status = SeasonalDropMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalDropMilestoneStatus.OPEN) }
    }
}
