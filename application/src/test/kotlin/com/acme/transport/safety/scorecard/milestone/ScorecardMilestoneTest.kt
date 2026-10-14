package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newScorecardMilestone("code-903", "title-118")
        assertEquals(ScorecardMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScorecardMilestone("code-903", "title-118")
        try {
            item.moveTo(ScorecardMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScorecardMilestone("code-903", "title-118")
        item.status = ScorecardMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ScorecardMilestoneStatus.CREATED) }
    }
}
