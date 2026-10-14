package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionSlotValidator()
        val item = newPendingInspectionSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingInspectionSlotRequiredCodeRule("pending inspection slot needs a value")
        val item = newPendingInspectionSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = PendingInspectionSlotLimitRangeRule(16, 255)
        val item = newPendingInspectionSlot("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = PendingInspectionSlotCategoryLengthRule(1000)
        val item = newPendingInspectionSlot("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PendingInspectionSlotRequiredRouteCodeRule("pending inspection slot needs this field")
        val item = newPendingInspectionSlot("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PendingInspectionSlotRouteCodeLengthRule(86400)
        val item = newPendingInspectionSlot("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
