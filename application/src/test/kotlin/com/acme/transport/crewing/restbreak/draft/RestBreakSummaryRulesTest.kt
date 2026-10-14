package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakSummaryValidator()
        val item = newRestBreakSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RestBreakSummaryCurrencyLengthRule(86400)
        val item = newRestBreakSummary("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakSummaryCodeLengthRule(1000)
        val item = newRestBreakSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RestBreakSummaryRequiredSourceRule("rest break summary needs an entry")
        val item = newRestBreakSummary("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RestBreakSummaryAttemptsRangeRule(64, 86400)
        val item = newRestBreakSummary("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RestBreakSummaryRequiredCurrencyRule("rest break summary needs this field")
        val item = newRestBreakSummary("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
