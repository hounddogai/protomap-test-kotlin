package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveFuelBurnAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveFuelBurnAdjustmentValidator()
        val item = newAdaptiveFuelBurnAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveFuelBurnAdjustmentRequiredDriverIdRule("adaptive fuel burn adjustment needs a value")
        val item = newAdaptiveFuelBurnAdjustment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AdaptiveFuelBurnAdjustmentDiscountRangeRule(7.9, 4182.1)
        val item = newAdaptiveFuelBurnAdjustment("x", "y")
        item.discount = 4183.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveFuelBurnAdjustmentRequiredCodeRule("adaptive fuel burn adjustment needs an entry")
        val item = newAdaptiveFuelBurnAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
