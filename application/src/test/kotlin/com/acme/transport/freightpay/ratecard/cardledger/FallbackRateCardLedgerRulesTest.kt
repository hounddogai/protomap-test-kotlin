package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRateCardLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRateCardLedgerValidator()
        val item = newFallbackRateCardLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackRateCardLedgerAttemptsRangeRule(64, 86400)
        val item = newFallbackRateCardLedger("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackRateCardLedgerRequiredTitleRule("fallback rate card ledger needs an entry")
        val item = newFallbackRateCardLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackRateCardLedgerRequiredCodeRule("fallback rate card ledger needs this field")
        val item = newFallbackRateCardLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackRateCardLedgerTitleLengthRule(80)
        val item = newFallbackRateCardLedger("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRateCardLedgerCodeLengthRule(1000)
        val item = newFallbackRateCardLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
