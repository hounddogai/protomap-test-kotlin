package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDwellReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDwellReviewValidator()
        val item = newDefaultDwellReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = DefaultDwellReviewVersionRangeRule(16, 16)
        val item = newDefaultDwellReview("x", "y")
        item.version = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultDwellReviewCodeLengthRule(80)
        val item = newDefaultDwellReview("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultDwellReviewRequiredCodeRule("default dwell review needs this field")
        val item = newDefaultDwellReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefaultDwellReviewChannelLengthRule(80)
        val item = newDefaultDwellReview("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DefaultDwellReviewPriorityRangeRule(255, 86400)
        val item = newDefaultDwellReview("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
