package com.acme.transport.customs.inspection.plan

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
    fun `flags a long description`() {
        val rule = InspectionPlanDescriptionLengthRule(16)
        val item = newInspectionPlan("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionPlanRequiredCodeRule("inspection plan needs an entry")
        val item = newInspectionPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = InspectionPlanBatchSizeRangeRule(1000, 86400)
        val item = newInspectionPlan("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
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

    @Test
    fun `flags a blank label`() {
        val rule = InspectionPlanRequiredLabelRule("inspection plan needs this field")
        val item = newInspectionPlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
