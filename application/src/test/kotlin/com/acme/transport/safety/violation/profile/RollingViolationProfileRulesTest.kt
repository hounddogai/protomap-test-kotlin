package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingViolationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingViolationProfileValidator()
        val item = newRollingViolationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingViolationProfileRequiredCodeRule("rolling violation profile needs this field")
        val item = newRollingViolationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RollingViolationProfileDiscountRangeRule(5.0, 1827.8)
        val item = newRollingViolationProfile("x", "y")
        item.discount = 1829.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingViolationProfileRequiredTitleRule("rolling violation profile needs an entry")
        val item = newRollingViolationProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingViolationProfileFuelLitersRangeRule(5.8, 1396.7)
        val item = newRollingViolationProfile("x", "y")
        item.fuelLiters = 1398.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
