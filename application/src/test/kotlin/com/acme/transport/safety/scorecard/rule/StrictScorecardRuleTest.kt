package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictScorecardRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictScorecardRule("code-554", "name-393")
        assertEquals(StrictScorecardRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictScorecardRule("code-554", "name-393")
        try {
            item.moveTo(StrictScorecardRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictScorecardRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictScorecardRule("code-554", "name-393")
        item.status = StrictScorecardRuleStatus.CLOSED
        assertFails("move") { item.moveTo(StrictScorecardRuleStatus.DRAFT) }
    }
}
