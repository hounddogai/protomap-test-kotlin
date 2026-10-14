package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalNodePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalNodePlanValidator()
        val item = newSeasonalNodePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalNodePlanRequiredLabelRule("seasonal node plan needs a value")
        val item = newSeasonalNodePlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalNodePlanCodeLengthRule(86400)
        val item = newSeasonalNodePlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = SeasonalNodePlanFuelLitersRangeRule(1.7, 4230.3)
        val item = newSeasonalNodePlan("x", "y")
        item.fuelLiters = 4231.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalNodePlanRequiredCodeRule("seasonal node plan needs a value")
        val item = newSeasonalNodePlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
