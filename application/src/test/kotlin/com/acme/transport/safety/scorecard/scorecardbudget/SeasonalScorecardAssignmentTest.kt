package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalScorecardAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        assertEquals(SeasonalScorecardAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        try {
            item.moveTo(SeasonalScorecardAssignmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalScorecardAssignmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        item.status = SeasonalScorecardAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalScorecardAssignmentStatus.OPEN) }
    }
}
