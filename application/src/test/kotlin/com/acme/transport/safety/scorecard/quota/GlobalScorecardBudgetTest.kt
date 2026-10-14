package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalScorecardBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalScorecardBudget("code-112", "name-281")
        assertEquals(GlobalScorecardBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalScorecardBudget("code-112", "name-281")
        try {
            item.moveTo(GlobalScorecardBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalScorecardBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalScorecardBudget("code-112", "name-281")
        item.status = GlobalScorecardBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalScorecardBudgetStatus.OPEN) }
    }
}
