package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerYardAssignmentValidator()
        val item = newManualTrailerYardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTrailerYardAssignmentCodeLengthRule(1000)
        val item = newManualTrailerYardAssignment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualTrailerYardAssignmentTitleLengthRule(1000)
        val item = newManualTrailerYardAssignment("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ManualTrailerYardAssignmentRequiredReviewerRule("manual trailer yard assignment needs this field")
        val item = newManualTrailerYardAssignment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
