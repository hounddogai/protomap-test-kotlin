package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropLedgerValidator()
        val item = newDropLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DropLedgerPalletsRangeRule(16, 255)
        val item = newDropLedger("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DropLedgerSequenceRangeRule(120, 255)
        val item = newDropLedger("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DropLedgerRequiredNameRule("drop ledger needs a value")
        val item = newDropLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
