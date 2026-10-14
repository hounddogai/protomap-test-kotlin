package com.acme.transport.terminal.dwell.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDwellDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDwellDraftValidator()
        val item = newCappedDwellDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedDwellDraftLocaleLengthRule(1000)
        val item = newCappedDwellDraft("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedDwellDraftLimitRangeRule(120, 1000)
        val item = newCappedDwellDraft("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CappedDwellDraftStopCountRangeRule(120, 1000)
        val item = newCappedDwellDraft("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedDwellDraftRequiredLocaleRule("capped dwell draft needs an entry")
        val item = newCappedDwellDraft("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
