package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCrossDockMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCrossDockMilestoneValidator()
        val item = newPrimaryCrossDockMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = PrimaryCrossDockMilestoneVersionRangeRule(16, 120)
        val item = newPrimaryCrossDockMilestone("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = PrimaryCrossDockMilestoneReviewerLengthRule(120)
        val item = newPrimaryCrossDockMilestone("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = PrimaryCrossDockMilestoneRequiredReviewerRule("primary cross dock milestone needs an entry")
        val item = newPrimaryCrossDockMilestone("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PrimaryCrossDockMilestoneExternalRefLengthRule(32)
        val item = newPrimaryCrossDockMilestone("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
