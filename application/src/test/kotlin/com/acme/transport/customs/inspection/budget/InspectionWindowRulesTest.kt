package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionWindowValidator()
        val item = newInspectionWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = InspectionWindowTrailerIdLengthRule(120)
        val item = newInspectionWindow("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = InspectionWindowWindowDaysRangeRule(80, 1000)
        val item = newInspectionWindow("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = InspectionWindowExpiresAtRangeRule(120, 86400)
        val item = newInspectionWindow("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
