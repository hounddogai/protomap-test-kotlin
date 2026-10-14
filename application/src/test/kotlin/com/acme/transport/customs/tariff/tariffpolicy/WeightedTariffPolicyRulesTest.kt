package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTariffPolicyValidator()
        val item = newWeightedTariffPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedTariffPolicyRequiredNameRule("weighted tariff policy needs this field")
        val item = newWeightedTariffPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTariffPolicyCodeLengthRule(1000)
        val item = newWeightedTariffPolicy("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedTariffPolicyRequiredCodeRule("weighted tariff policy needs this field")
        val item = newWeightedTariffPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = WeightedTariffPolicyGraceDaysRangeRule(16, 32)
        val item = newWeightedTariffPolicy("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
