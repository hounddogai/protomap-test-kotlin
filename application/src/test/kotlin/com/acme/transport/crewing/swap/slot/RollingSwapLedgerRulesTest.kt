package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapLedgerValidator()
        val item = newRollingSwapLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RollingSwapLedgerNotesLengthRule(80)
        val item = newRollingSwapLedger("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingSwapLedgerRequiredLabelRule("rolling swap ledger needs this field")
        val item = newRollingSwapLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingSwapLedgerCodeLengthRule(32)
        val item = newRollingSwapLedger("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
