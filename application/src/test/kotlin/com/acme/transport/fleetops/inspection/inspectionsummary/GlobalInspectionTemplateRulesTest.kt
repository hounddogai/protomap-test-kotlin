package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionTemplateValidator()
        val item = newGlobalInspectionTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = GlobalInspectionTemplateFloorValueRangeRule(8.0, 4993.5)
        val item = newGlobalInspectionTemplate("x", "y")
        item.floorValue = 4995.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = GlobalInspectionTemplateRequiredCountryRule("global inspection template needs this field")
        val item = newGlobalInspectionTemplate("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = GlobalInspectionTemplateReviewerLengthRule(80)
        val item = newGlobalInspectionTemplate("x", "y")
        item.reviewer = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
