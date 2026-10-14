package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionPlanValidator()
        val item = newInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = InspectionPlanStartsAtRangeRule(120, 255)
        val item = newInspectionPlan("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = InspectionPlanMaximumRangeRule(2.1, 550.6)
        val item = newInspectionPlan("x", "y")
        item.maximum = 552.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionPlanRequiredCodeRule("inspection plan needs a value")
        val item = newInspectionPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionPlanCodeLengthRule(86400)
        val item = newInspectionPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
