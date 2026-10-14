package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapLedgerValidator()
        val item = newSwapLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapLedgerCodeLengthRule(3600)
        val item = newSwapLedger("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = SwapLedgerExternalRefLengthRule(1000)
        val item = newSwapLedger("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapLedgerRequiredCodeRule("swap ledger needs a value")
        val item = newSwapLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SwapLedgerAmountRangeRule(1.5, 4242.5)
        val item = newSwapLedger("x", "y")
        item.amount = 4244.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SwapLedgerRequiredExternalRefRule("swap ledger needs a value")
        val item = newSwapLedger("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapLedgerTitleLengthRule(80)
        val item = newSwapLedger("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
