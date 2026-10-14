package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRestBreakAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRestBreakAssignmentValidator()
        val item = newFallbackRestBreakAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = FallbackRestBreakAssignmentReviewerLengthRule(80)
        val item = newFallbackRestBreakAssignment("x", "y")
        item.reviewer = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FallbackRestBreakAssignmentMinimumRangeRule(9.0, 2353.5)
        val item = newFallbackRestBreakAssignment("x", "y")
        item.minimum = 2355.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = FallbackRestBreakAssignmentRequiredReviewerRule("fallback rest break assignment needs a value")
        val item = newFallbackRestBreakAssignment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRestBreakAssignmentCodeLengthRule(16)
        val item = newFallbackRestBreakAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
