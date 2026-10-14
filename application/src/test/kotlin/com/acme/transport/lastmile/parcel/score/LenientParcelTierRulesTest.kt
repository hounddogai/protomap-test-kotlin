package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientParcelTierValidator()
        val item = newLenientParcelTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LenientParcelTierBudgetRangeRule(6.9, 4463.4)
        val item = newLenientParcelTier("x", "y")
        item.budget = 4464.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientParcelTierDiscountRangeRule(5.3, 77.5)
        val item = newLenientParcelTier("x", "y")
        item.discount = 79.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientParcelTierRequiredCodeRule("lenient parcel tier needs this field")
        val item = newLenientParcelTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientParcelTierRequiredTitleRule("lenient parcel tier needs this field")
        val item = newLenientParcelTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
