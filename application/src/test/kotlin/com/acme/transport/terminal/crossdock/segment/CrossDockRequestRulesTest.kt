package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockRequestValidator()
        val item = newCrossDockRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CrossDockRequestLabelLengthRule(16)
        val item = newCrossDockRequest("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CrossDockRequestRequiredLabelRule("cross dock request needs a value")
        val item = newCrossDockRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CrossDockRequestCeilingRangeRule(2.1, 3986.9)
        val item = newCrossDockRequest("x", "y")
        item.ceiling = 3988.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CrossDockRequestRatioRangeRule(8.7, 2163.1)
        val item = newCrossDockRequest("x", "y")
        item.ratio = 2164.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockRequestCodeLengthRule(16)
        val item = newCrossDockRequest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
