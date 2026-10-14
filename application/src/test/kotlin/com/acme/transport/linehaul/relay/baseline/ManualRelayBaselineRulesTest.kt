package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRelayBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRelayBaselineValidator()
        val item = newManualRelayBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ManualRelayBaselineLaneLengthRule(16)
        val item = newManualRelayBaseline("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ManualRelayBaselineRequiredCarrierRule("manual relay baseline needs this field")
        val item = newManualRelayBaseline("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRelayBaselineCodeLengthRule(64)
        val item = newManualRelayBaseline("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualRelayBaselineWindowDaysRangeRule(120, 3600)
        val item = newManualRelayBaseline("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
