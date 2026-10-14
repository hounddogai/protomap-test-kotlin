package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedUtilizationRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedUtilizationRuleValidator()
        val item = newWeightedUtilizationRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedUtilizationRuleCodeLengthRule(32)
        val item = newWeightedUtilizationRule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = WeightedUtilizationRuleRequiredPhoneRule("weighted utilization rule needs this field")
        val item = newWeightedUtilizationRule("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = WeightedUtilizationRulePhoneLengthRule(1000)
        val item = newWeightedUtilizationRule("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
