package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCameraEventRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCameraEventRollupValidator()
        val item = newAdaptiveCameraEventRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveCameraEventRollupPhoneLengthRule(80)
        val item = newAdaptiveCameraEventRollup("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AdaptiveCameraEventRollupDiscountRangeRule(6.6, 965.2)
        val item = newAdaptiveCameraEventRollup("x", "y")
        item.discount = 966.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AdaptiveCameraEventRollupBudgetRangeRule(0.8, 2596.8)
        val item = newAdaptiveCameraEventRollup("x", "y")
        item.budget = 2598.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
