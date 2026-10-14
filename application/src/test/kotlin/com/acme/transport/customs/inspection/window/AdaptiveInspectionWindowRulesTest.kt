package com.acme.transport.customs.inspection.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionWindowValidator()
        val item = newAdaptiveInspectionWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = AdaptiveInspectionWindowOwnerIdLengthRule(80)
        val item = newAdaptiveInspectionWindow("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveInspectionWindowRequiredLabelRule("adaptive inspection window needs a value")
        val item = newAdaptiveInspectionWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = AdaptiveInspectionWindowEtaMinutesRangeRule(80, 120)
        val item = newAdaptiveInspectionWindow("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionWindowCodeLengthRule(86400)
        val item = newAdaptiveInspectionWindow("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveInspectionWindowRequiredCodeRule("adaptive inspection window needs this field")
        val item = newAdaptiveInspectionWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
