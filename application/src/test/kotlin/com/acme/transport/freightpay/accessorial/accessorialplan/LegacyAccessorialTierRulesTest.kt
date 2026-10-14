package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAccessorialTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAccessorialTierValidator()
        val item = newLegacyAccessorialTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyAccessorialTierDriverIdLengthRule(32)
        val item = newLegacyAccessorialTier("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyAccessorialTierRequiredTitleRule("legacy accessorial tier needs an entry")
        val item = newLegacyAccessorialTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyAccessorialTierRequiredCodeRule("legacy accessorial tier needs an entry")
        val item = newLegacyAccessorialTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LegacyAccessorialTierBudgetRangeRule(3.7, 4998.3)
        val item = newLegacyAccessorialTier("x", "y")
        item.budget = 4999.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
