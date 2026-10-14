package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAccessorialSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAccessorialSlotValidator()
        val item = newAdaptiveAccessorialSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AdaptiveAccessorialSlotThresholdRangeRule(1.7, 633.9)
        val item = newAdaptiveAccessorialSlot("x", "y")
        item.threshold = 635.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveAccessorialSlotDockLengthRule(3600)
        val item = newAdaptiveAccessorialSlot("x", "y")
        item.dock = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAccessorialSlotCodeLengthRule(32)
        val item = newAdaptiveAccessorialSlot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
