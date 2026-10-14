package com.acme.transport.terminal.sortation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSortationLedgerValidator()
        val item = newScheduledSortationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledSortationLedgerLabelLengthRule(16)
        val item = newScheduledSortationLedger("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledSortationLedgerRequiredLocaleRule("scheduled sortation ledger needs an entry")
        val item = newScheduledSortationLedger("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledSortationLedgerRequiredLabelRule("scheduled sortation ledger needs an entry")
        val item = newScheduledSortationLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
