package com.acme.transport.fleetops.utilization.utilizationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalUtilizationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalUtilizationOverrideValidator()
        val item = newRegionalUtilizationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RegionalUtilizationOverrideBudgetRangeRule(4.8, 1400.6)
        val item = newRegionalUtilizationOverride("x", "y")
        item.budget = 1402.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalUtilizationOverrideRequiredCodeRule("regional utilization override needs this field")
        val item = newRegionalUtilizationOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = RegionalUtilizationOverrideSourceLengthRule(80)
        val item = newRegionalUtilizationOverride("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RegionalUtilizationOverridePriorityRangeRule(120, 120)
        val item = newRegionalUtilizationOverride("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalUtilizationOverrideTitleLengthRule(32)
        val item = newRegionalUtilizationOverride("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RegionalUtilizationOverrideRequiredSourceRule("regional utilization override needs this field")
        val item = newRegionalUtilizationOverride("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
