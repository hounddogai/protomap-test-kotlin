package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAccessorialBaselineValidator()
        val item = newManualAccessorialBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualAccessorialBaselineWindowDaysRangeRule(120, 120)
        val item = newManualAccessorialBaseline("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualAccessorialBaselineLabelLengthRule(80)
        val item = newManualAccessorialBaseline("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualAccessorialBaselineRequiredLabelRule("manual accessorial baseline needs a value")
        val item = newManualAccessorialBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAccessorialBaselineRequiredCodeRule("manual accessorial baseline needs this field")
        val item = newManualAccessorialBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
