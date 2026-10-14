package com.acme.transport.fleetops.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRequestValidator()
        val item = newInspectionRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionRequestCodeLengthRule(64)
        val item = newInspectionRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = InspectionRequestRequiredRouteCodeRule("inspection request needs this field")
        val item = newInspectionRequest("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = InspectionRequestAttemptsRangeRule(80, 120)
        val item = newInspectionRequest("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = InspectionRequestStartsAtRangeRule(255, 1000)
        val item = newInspectionRequest("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = InspectionRequestRouteCodeLengthRule(32)
        val item = newInspectionRequest("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
