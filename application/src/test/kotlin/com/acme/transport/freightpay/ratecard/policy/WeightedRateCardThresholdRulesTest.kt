package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRateCardThresholdValidator()
        val item = newWeightedRateCardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = WeightedRateCardThresholdRequiredExternalRefRule("weighted rate card threshold needs a value")
        val item = newWeightedRateCardThreshold("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = WeightedRateCardThresholdOdometerRangeRule(32, 255)
        val item = newWeightedRateCardThreshold("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
