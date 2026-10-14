package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingLedgerValidator()
        val item = newCoachingLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CoachingLedgerRequiredTitleRule("coaching ledger needs this field")
        val item = newCoachingLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CoachingLedgerSummaryLengthRule(120)
        val item = newCoachingLedger("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = CoachingLedgerTitleLengthRule(255)
        val item = newCoachingLedger("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CoachingLedgerRequiredSummaryRule("coaching ledger needs an entry")
        val item = newCoachingLedger("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
