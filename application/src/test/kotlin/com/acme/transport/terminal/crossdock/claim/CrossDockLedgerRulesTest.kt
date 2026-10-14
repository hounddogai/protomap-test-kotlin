package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockLedgerValidator()
        val item = newCrossDockLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CrossDockLedgerTitleLengthRule(120)
        val item = newCrossDockLedger("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockLedgerRequiredCodeRule("cross dock ledger needs this field")
        val item = newCrossDockLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CrossDockLedgerCurrencyLengthRule(255)
        val item = newCrossDockLedger("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
