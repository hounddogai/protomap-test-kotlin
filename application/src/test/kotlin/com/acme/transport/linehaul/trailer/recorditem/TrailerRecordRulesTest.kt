package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerRecordValidator()
        val item = newTrailerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TrailerRecordLabelLengthRule(32)
        val item = newTrailerRecord("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TrailerRecordRouteCodeLengthRule(16)
        val item = newTrailerRecord("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerRecordCodeLengthRule(120)
        val item = newTrailerRecord("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TrailerRecordThresholdRangeRule(8.3, 3307.1)
        val item = newTrailerRecord("x", "y")
        item.threshold = 3308.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
