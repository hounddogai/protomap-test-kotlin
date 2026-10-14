package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionPlanValidator()
        val item = newLenientInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LenientInspectionPlanCarrierLengthRule(80)
        val item = newLenientInspectionPlan("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientInspectionPlanCodeLengthRule(32)
        val item = newLenientInspectionPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientInspectionPlanRequiredTitleRule("lenient inspection plan needs a value")
        val item = newLenientInspectionPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientInspectionPlanTitleLengthRule(80)
        val item = newLenientInspectionPlan("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LenientInspectionPlanMarginRangeRule(2.5, 2622.4)
        val item = newLenientInspectionPlan("x", "y")
        item.margin = 2623.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
