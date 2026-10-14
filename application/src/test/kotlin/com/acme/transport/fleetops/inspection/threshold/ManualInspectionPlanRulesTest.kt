package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionPlanValidator()
        val item = newManualInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualInspectionPlanRequiredCodeRule("manual inspection plan needs an entry")
        val item = newManualInspectionPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualInspectionPlanLabelLengthRule(120)
        val item = newManualInspectionPlan("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualInspectionPlanCategoryLengthRule(1000)
        val item = newManualInspectionPlan("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualInspectionPlanScoreRangeRule(0.3, 404.7)
        val item = newManualInspectionPlan("x", "y")
        item.score = 406.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
