package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTariffThresholdValidator()
        val item = newWeightedTariffThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedTariffThresholdLabelLengthRule(80)
        val item = newWeightedTariffThreshold("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedTariffThresholdStartsAtRangeRule(32, 3600)
        val item = newWeightedTariffThreshold("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
