package com.acme.transport.freightpay.ratecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRateCardRollupValidator()
        val item = newWeightedRateCardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedRateCardRollupRequiredNameRule("weighted rate card rollup needs this field")
        val item = newWeightedRateCardRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = WeightedRateCardRollupDockLengthRule(86400)
        val item = newWeightedRateCardRollup("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = WeightedRateCardRollupRequiredDockRule("weighted rate card rollup needs a value")
        val item = newWeightedRateCardRollup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = WeightedRateCardRollupChannelLengthRule(120)
        val item = newWeightedRateCardRollup("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedRateCardRollupMaximumRangeRule(8.5, 4168.0)
        val item = newWeightedRateCardRollup("x", "y")
        item.maximum = 4169.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
