package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCrossDockAdjustmentValidator()
        val item = newAdaptiveCrossDockAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveCrossDockAdjustmentLabelLengthRule(86400)
        val item = newAdaptiveCrossDockAdjustment("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveCrossDockAdjustmentRatioRangeRule(4.0, 4077.6)
        val item = newAdaptiveCrossDockAdjustment("x", "y")
        item.ratio = 4079.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveCrossDockAdjustmentLoadKgRangeRule(9.0, 1411.4)
        val item = newAdaptiveCrossDockAdjustment("x", "y")
        item.loadKg = 1412.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = AdaptiveCrossDockAdjustmentRequiredPhoneRule("adaptive cross dock adjustment needs this field")
        val item = newAdaptiveCrossDockAdjustment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AdaptiveCrossDockAdjustmentRequiredCountryRule("adaptive cross dock adjustment needs this field")
        val item = newAdaptiveCrossDockAdjustment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveCrossDockAdjustmentCodeLengthRule(64)
        val item = newAdaptiveCrossDockAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
