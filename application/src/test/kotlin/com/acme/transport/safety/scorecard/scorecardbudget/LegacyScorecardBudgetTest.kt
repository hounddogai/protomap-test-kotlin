package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyScorecardBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyScorecardBudget("code-303", "name-563")
        assertEquals(LegacyScorecardBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyScorecardBudget("code-303", "name-563")
        try {
            item.moveTo(LegacyScorecardBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyScorecardBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyScorecardBudget("code-303", "name-563")
        item.status = LegacyScorecardBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyScorecardBudgetStatus.PROPOSED) }
    }
}
