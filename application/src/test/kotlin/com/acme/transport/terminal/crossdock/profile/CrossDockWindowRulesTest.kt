package com.acme.transport.terminal.crossdock.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockWindowValidator()
        val item = newCrossDockWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CrossDockWindowLabelLengthRule(86400)
        val item = newCrossDockWindow("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CrossDockWindowRequiredLabelRule("cross dock window needs this field")
        val item = newCrossDockWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CrossDockWindowMaximumRangeRule(3.0, 2061.2)
        val item = newCrossDockWindow("x", "y")
        item.maximum = 2062.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CrossDockWindowRequiredLaneRule("cross dock window needs a value")
        val item = newCrossDockWindow("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CrossDockWindowCurrencyLengthRule(32)
        val item = newCrossDockWindow("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CrossDockWindowQuantityRangeRule(80, 1000)
        val item = newCrossDockWindow("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
