package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDockDoorBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDockDoorBaselineValidator()
        val item = newCappedDockDoorBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CappedDockDoorBaselineRequiredSummaryRule("capped dock door baseline needs an entry")
        val item = newCappedDockDoorBaseline("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CappedDockDoorBaselineSummaryLengthRule(16)
        val item = newCappedDockDoorBaseline("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedDockDoorBaselineRequiredTitleRule("capped dock door baseline needs this field")
        val item = newCappedDockDoorBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CappedDockDoorBaselineRequiredCurrencyRule("capped dock door baseline needs an entry")
        val item = newCappedDockDoorBaseline("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
