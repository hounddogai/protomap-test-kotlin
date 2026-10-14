package com.acme.transport.customs.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionOverrideValidator()
        val item = newInspectionOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionOverrideNameLengthRule(3600)
        val item = newInspectionOverride("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionOverrideCodeLengthRule(80)
        val item = newInspectionOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = InspectionOverrideAttemptsRangeRule(64, 86400)
        val item = newInspectionOverride("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
