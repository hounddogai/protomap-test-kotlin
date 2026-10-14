package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardRuleTest {
    @Test
    fun `starts in open`() {
        val item = newScorecardRule("code-300", "title-640")
        assertEquals(ScorecardRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScorecardRule("code-300", "title-640")
        try {
            item.moveTo(ScorecardRuleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardRuleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScorecardRule("code-300", "title-640")
        item.status = ScorecardRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScorecardRuleStatus.OPEN) }
    }
}
