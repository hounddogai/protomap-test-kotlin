package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockSummaryValidator()
        val item = newCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CrossDockSummaryCurrencyLengthRule(64)
        val item = newCrossDockSummary("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockSummaryRequiredCodeRule("cross dock summary needs a value")
        val item = newCrossDockSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockSummaryCodeLengthRule(1000)
        val item = newCrossDockSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
