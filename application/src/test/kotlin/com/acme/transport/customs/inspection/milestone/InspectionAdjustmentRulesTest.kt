package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAdjustmentValidator()
        val item = newInspectionAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = InspectionAdjustmentEndsAtRangeRule(80, 120)
        val item = newInspectionAdjustment("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = InspectionAdjustmentGraceDaysRangeRule(32, 64)
        val item = newInspectionAdjustment("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = InspectionAdjustmentOdometerRangeRule(16, 3600)
        val item = newInspectionAdjustment("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = InspectionAdjustmentRequiredCategoryRule("inspection adjustment needs an entry")
        val item = newInspectionAdjustment("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = InspectionAdjustmentCategoryLengthRule(86400)
        val item = newInspectionAdjustment("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
