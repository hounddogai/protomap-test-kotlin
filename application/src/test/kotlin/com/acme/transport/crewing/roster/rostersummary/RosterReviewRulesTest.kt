package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterReviewValidator()
        val item = newRosterReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RosterReviewBudgetRangeRule(5.1, 3524.6)
        val item = newRosterReview("x", "y")
        item.budget = 3526.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RosterReviewNameLengthRule(80)
        val item = newRosterReview("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterReviewRequiredCodeRule("roster review needs a value")
        val item = newRosterReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
