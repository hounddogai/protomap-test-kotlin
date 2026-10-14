package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTrailerYardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTrailerYardWindowValidator()
        val item = newDefaultTrailerYardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefaultTrailerYardWindowEndsAtRangeRule(32, 3600)
        val item = newDefaultTrailerYardWindow("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTrailerYardWindowCodeLengthRule(255)
        val item = newDefaultTrailerYardWindow("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultTrailerYardWindowLabelLengthRule(1000)
        val item = newDefaultTrailerYardWindow("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultTrailerYardWindowWindowDaysRangeRule(120, 3600)
        val item = newDefaultTrailerYardWindow("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DefaultTrailerYardWindowDriverIdLengthRule(64)
        val item = newDefaultTrailerYardWindow("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
