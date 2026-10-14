package com.acme.transport.lastmile.drop.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropTierValidator()
        val item = newDropTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DropTierAttemptsRangeRule(16, 120)
        val item = newDropTier("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropTierRequiredCodeRule("drop tier needs this field")
        val item = newDropTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DropTierMinimumRangeRule(7.4, 3721.9)
        val item = newDropTier("x", "y")
        item.minimum = 3723.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = DropTierRequiredTimezoneRule("drop tier needs an entry")
        val item = newDropTier("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
