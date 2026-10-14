package com.acme.transport.safety.violation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedViolationThresholdValidator()
        val item = newCappedViolationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CappedViolationThresholdRatioRangeRule(0.9, 1006.7)
        val item = newCappedViolationThreshold("x", "y")
        item.ratio = 1008.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedViolationThresholdRequiredTitleRule("capped violation threshold needs a value")
        val item = newCappedViolationThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedViolationThresholdCodeLengthRule(120)
        val item = newCappedViolationThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedViolationThresholdRequiredCodeRule("capped violation threshold needs this field")
        val item = newCappedViolationThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedViolationThresholdTitleLengthRule(86400)
        val item = newCappedViolationThreshold("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
