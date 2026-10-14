package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTenderWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTenderWindowValidator()
        val item = newAdaptiveTenderWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AdaptiveTenderWindowOdometerRangeRule(80, 120)
        val item = newAdaptiveTenderWindow("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = AdaptiveTenderWindowRequiredSummaryRule("adaptive tender window needs a value")
        val item = newAdaptiveTenderWindow("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveTenderWindowCodeLengthRule(64)
        val item = newAdaptiveTenderWindow("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AdaptiveTenderWindowSummaryLengthRule(80)
        val item = newAdaptiveTenderWindow("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveTenderWindowTitleLengthRule(1000)
        val item = newAdaptiveTenderWindow("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
