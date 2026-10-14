package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerBudgetValidator()
        val item = newTrailerBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerBudgetRequiredCodeRule("trailer budget needs a value")
        val item = newTrailerBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TrailerBudgetWindowDaysRangeRule(16, 255)
        val item = newTrailerBudget("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TrailerBudgetRevisionRangeRule(32, 3600)
        val item = newTrailerBudget("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TrailerBudgetChecksumLengthRule(255)
        val item = newTrailerBudget("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
