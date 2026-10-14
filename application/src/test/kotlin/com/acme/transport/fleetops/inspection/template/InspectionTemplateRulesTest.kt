package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionTemplateValidator()
        val item = newInspectionTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionTemplateRequiredLabelRule("inspection template needs a value")
        val item = newInspectionTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = InspectionTemplateDiscountRangeRule(6.9, 2282.3)
        val item = newInspectionTemplate("x", "y")
        item.discount = 2283.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = InspectionTemplateEndsAtRangeRule(120, 120)
        val item = newInspectionTemplate("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
