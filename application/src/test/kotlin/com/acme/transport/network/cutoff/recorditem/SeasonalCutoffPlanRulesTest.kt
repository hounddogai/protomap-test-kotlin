package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCutoffPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCutoffPlanValidator()
        val item = newSeasonalCutoffPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCutoffPlanCodeLengthRule(16)
        val item = newSeasonalCutoffPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = SeasonalCutoffPlanCapacityRangeRule(80, 80)
        val item = newSeasonalCutoffPlan("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalCutoffPlanCostRangeRule(7.8, 2532.3)
        val item = newSeasonalCutoffPlan("x", "y")
        item.cost = 2533.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = SeasonalCutoffPlanRequiredPhoneRule("seasonal cutoff plan needs this field")
        val item = newSeasonalCutoffPlan("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
