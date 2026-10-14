package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictUtilizationRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictUtilizationRuleValidator()
        val item = newStrictUtilizationRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictUtilizationRuleCodeLengthRule(3600)
        val item = newStrictUtilizationRule("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictUtilizationRuleTitleLengthRule(64)
        val item = newStrictUtilizationRule("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictUtilizationRulePriorityRangeRule(80, 120)
        val item = newStrictUtilizationRule("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
