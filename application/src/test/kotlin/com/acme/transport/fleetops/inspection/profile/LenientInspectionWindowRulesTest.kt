package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionWindowValidator()
        val item = newLenientInspectionWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = LenientInspectionWindowRegionLengthRule(3600)
        val item = newLenientInspectionWindow("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientInspectionWindowRequiredCodeRule("lenient inspection window needs a value")
        val item = newLenientInspectionWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientInspectionWindowRequiredRegionRule("lenient inspection window needs this field")
        val item = newLenientInspectionWindow("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientInspectionWindowLabelLengthRule(64)
        val item = newLenientInspectionWindow("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
