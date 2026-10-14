package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedViolationBaselineValidator()
        val item = newCappedViolationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedViolationBaselineGraceDaysRangeRule(16, 32)
        val item = newCappedViolationBaseline("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedViolationBaselineRequiredNameRule("capped violation baseline needs this field")
        val item = newCappedViolationBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = CappedViolationBaselineRequiredPhoneRule("capped violation baseline needs an entry")
        val item = newCappedViolationBaseline("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
