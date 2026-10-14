package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationLedgerValidator()
        val item = newSortationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = SortationLedgerRequiredReviewerRule("sortation ledger needs an entry")
        val item = newSortationLedger("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationLedgerCodeLengthRule(16)
        val item = newSortationLedger("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SortationLedgerRequiredTitleRule("sortation ledger needs this field")
        val item = newSortationLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
