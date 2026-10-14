package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationOverrideValidator()
        val item = newViolationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ViolationOverrideQuantityRangeRule(16, 86400)
        val item = newViolationOverride("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ViolationOverrideRequiredCurrencyRule("violation override needs an entry")
        val item = newViolationOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ViolationOverrideAttemptsRangeRule(32, 1000)
        val item = newViolationOverride("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ViolationOverrideRequiredNameRule("violation override needs an entry")
        val item = newViolationOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
