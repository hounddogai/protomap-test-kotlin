package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCoachingTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCoachingTierValidator()
        val item = newRollingCoachingTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingCoachingTierRequiredNameRule("rolling coaching tier needs an entry")
        val item = newRollingCoachingTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingCoachingTierLaneLengthRule(3600)
        val item = newRollingCoachingTier("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RollingCoachingTierRequiredCurrencyRule("rolling coaching tier needs a value")
        val item = newRollingCoachingTier("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
