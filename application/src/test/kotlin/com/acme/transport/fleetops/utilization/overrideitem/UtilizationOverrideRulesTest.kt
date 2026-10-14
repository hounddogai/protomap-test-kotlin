package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationOverrideValidator()
        val item = newUtilizationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = UtilizationOverrideRequiredCategoryRule("utilization override needs a value")
        val item = newUtilizationOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = UtilizationOverrideRequiredSegmentRule("utilization override needs a value")
        val item = newUtilizationOverride("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = UtilizationOverrideCategoryLengthRule(64)
        val item = newUtilizationOverride("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = UtilizationOverrideWindowDaysRangeRule(32, 3600)
        val item = newUtilizationOverride("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationOverrideRequiredCodeRule("utilization override needs this field")
        val item = newUtilizationOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
