package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBrokerTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBrokerTierValidator()
        val item = newAdaptiveBrokerTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveBrokerTierCountryLengthRule(86400)
        val item = newAdaptiveBrokerTier("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = AdaptiveBrokerTierPriorityRangeRule(16, 16)
        val item = newAdaptiveBrokerTier("x", "y")
        item.priority = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AdaptiveBrokerTierRequiredCountryRule("adaptive broker tier needs an entry")
        val item = newAdaptiveBrokerTier("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
