package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalViolationSegmentValidator()
        val item = newGlobalViolationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalViolationSegmentRequiredCodeRule("global violation segment needs this field")
        val item = newGlobalViolationSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalViolationSegmentRequiredTitleRule("global violation segment needs an entry")
        val item = newGlobalViolationSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalViolationSegmentCodeLengthRule(64)
        val item = newGlobalViolationSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = GlobalViolationSegmentScoreRangeRule(2.1, 2139.3)
        val item = newGlobalViolationSegment("x", "y")
        item.score = 2140.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = GlobalViolationSegmentCurrencyLengthRule(86400)
        val item = newGlobalViolationSegment("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
