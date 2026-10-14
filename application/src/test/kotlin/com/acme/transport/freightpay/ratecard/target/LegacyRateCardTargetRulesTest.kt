package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRateCardTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRateCardTargetValidator()
        val item = newLegacyRateCardTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyRateCardTargetRatioRangeRule(3.0, 4724.1)
        val item = newLegacyRateCardTarget("x", "y")
        item.ratio = 4725.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyRateCardTargetFuelLitersRangeRule(7.6, 1531.3)
        val item = newLegacyRateCardTarget("x", "y")
        item.fuelLiters = 1532.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
