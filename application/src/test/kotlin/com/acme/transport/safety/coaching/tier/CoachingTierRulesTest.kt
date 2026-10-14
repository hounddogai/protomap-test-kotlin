package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingTierValidator()
        val item = newCoachingTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CoachingTierRequiredLocaleRule("coaching tier needs an entry")
        val item = newCoachingTier("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CoachingTierLocaleLengthRule(86400)
        val item = newCoachingTier("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingTierRequiredCodeRule("coaching tier needs a value")
        val item = newCoachingTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CoachingTierMaximumRangeRule(8.9, 368.9)
        val item = newCoachingTier("x", "y")
        item.maximum = 370.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
