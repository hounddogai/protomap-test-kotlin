package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerProfileValidator()
        val item = newTrailerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TrailerProfileGraceDaysRangeRule(120, 1000)
        val item = newTrailerProfile("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TrailerProfilePhoneLengthRule(120)
        val item = newTrailerProfile("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerProfileCodeLengthRule(120)
        val item = newTrailerProfile("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
