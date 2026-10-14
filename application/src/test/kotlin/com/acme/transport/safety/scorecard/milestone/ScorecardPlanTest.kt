package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardPlanTest {
    @Test
    fun `starts in created`() {
        val item = newScorecardPlan("code-892", "label-342")
        assertEquals(ScorecardPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScorecardPlan("code-892", "label-342")
        try {
            item.moveTo(ScorecardPlanStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardPlanStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScorecardPlan("code-892", "label-342")
        item.status = ScorecardPlanStatus.RETIRED
        assertFails("move") { item.moveTo(ScorecardPlanStatus.CREATED) }
    }
}
