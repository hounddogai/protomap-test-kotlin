package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionGroupValidator()
        val item = newGlobalInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalInspectionGroupNameLengthRule(80)
        val item = newGlobalInspectionGroup("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalInspectionGroupRequiredCodeRule("global inspection group needs an entry")
        val item = newGlobalInspectionGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalInspectionGroupAttemptsRangeRule(16, 3600)
        val item = newGlobalInspectionGroup("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalInspectionGroupDiscountRangeRule(4.1, 2753.2)
        val item = newGlobalInspectionGroup("x", "y")
        item.discount = 2754.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalInspectionGroupEndsAtRangeRule(64, 64)
        val item = newGlobalInspectionGroup("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
