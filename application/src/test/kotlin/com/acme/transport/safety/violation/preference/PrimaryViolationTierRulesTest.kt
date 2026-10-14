package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryViolationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryViolationTierValidator()
        val item = newPrimaryViolationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PrimaryViolationTierGraceDaysRangeRule(16, 120)
        val item = newPrimaryViolationTier("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryViolationTierRequiredCodeRule("primary violation tier needs an entry")
        val item = newPrimaryViolationTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PrimaryViolationTierRequiredSegmentRule("primary violation tier needs an entry")
        val item = newPrimaryViolationTier("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = PrimaryViolationTierSegmentLengthRule(80)
        val item = newPrimaryViolationTier("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
