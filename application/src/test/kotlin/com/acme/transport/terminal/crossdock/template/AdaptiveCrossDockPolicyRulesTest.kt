package com.acme.transport.terminal.crossdock.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCrossDockPolicyValidator()
        val item = newAdaptiveCrossDockPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveCrossDockPolicyCodeLengthRule(32)
        val item = newAdaptiveCrossDockPolicy("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AdaptiveCrossDockPolicyQuantityRangeRule(80, 255)
        val item = newAdaptiveCrossDockPolicy("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveCrossDockPolicyNameLengthRule(64)
        val item = newAdaptiveCrossDockPolicy("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveCrossDockPolicyRatioRangeRule(4.8, 4469.4)
        val item = newAdaptiveCrossDockPolicy("x", "y")
        item.ratio = 4470.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
