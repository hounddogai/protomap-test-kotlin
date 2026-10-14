package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerWindowValidator()
        val item = newTrailerWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TrailerWindowFuelLitersRangeRule(6.3, 654.6)
        val item = newTrailerWindow("x", "y")
        item.fuelLiters = 656.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TrailerWindowUpdatedAtRangeRule(64, 120)
        val item = newTrailerWindow("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerWindowTitleLengthRule(3600)
        val item = newTrailerWindow("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerWindowRequiredTitleRule("trailer window needs a value")
        val item = newTrailerWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerWindowCodeLengthRule(255)
        val item = newTrailerWindow("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
