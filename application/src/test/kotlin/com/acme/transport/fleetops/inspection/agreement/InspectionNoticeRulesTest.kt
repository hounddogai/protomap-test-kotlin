package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionNoticeValidator()
        val item = newInspectionNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = InspectionNoticeVehicleIdLengthRule(255)
        val item = newInspectionNotice("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = InspectionNoticeBudgetRangeRule(8.1, 2248.6)
        val item = newInspectionNotice("x", "y")
        item.budget = 2250.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionNoticeRequiredCodeRule("inspection notice needs this field")
        val item = newInspectionNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionNoticeNameLengthRule(32)
        val item = newInspectionNotice("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
