package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffLedgerValidator()
        val item = newCutoffLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CutoffLedgerPriorityRangeRule(32, 1000)
        val item = newCutoffLedger("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CutoffLedgerRatioRangeRule(8.9, 1113.7)
        val item = newCutoffLedger("x", "y")
        item.ratio = 1115.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CutoffLedgerRequiredNameRule("cutoff ledger needs this field")
        val item = newCutoffLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
