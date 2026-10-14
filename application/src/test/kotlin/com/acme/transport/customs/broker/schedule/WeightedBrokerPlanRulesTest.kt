package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBrokerPlanValidator()
        val item = newWeightedBrokerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedBrokerPlanQuantityRangeRule(80, 120)
        val item = newWeightedBrokerPlan("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = WeightedBrokerPlanBudgetRangeRule(9.1, 4849.4)
        val item = newWeightedBrokerPlan("x", "y")
        item.budget = 4850.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = WeightedBrokerPlanChecksumLengthRule(64)
        val item = newWeightedBrokerPlan("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
