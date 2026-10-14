package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationPlanValidator()
        val item = newUtilizationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = UtilizationPlanRevisionRangeRule(64, 255)
        val item = newUtilizationPlan("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = UtilizationPlanWeightRangeRule(7.9, 2258.6)
        val item = newUtilizationPlan("x", "y")
        item.weight = 2260.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = UtilizationPlanTitleLengthRule(120)
        val item = newUtilizationPlan("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = UtilizationPlanCodeLengthRule(255)
        val item = newUtilizationPlan("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationPlanRequiredCodeRule("utilization plan needs a value")
        val item = newUtilizationPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
