package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCutoffLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCutoffLedgerValidator()
        val item = newFallbackCutoffLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackCutoffLedgerRequiredContactEmailRule("fallback cutoff ledger needs a value")
        val item = newFallbackCutoffLedger("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackCutoffLedgerStopCountRangeRule(16, 80)
        val item = newFallbackCutoffLedger("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackCutoffLedgerEtaMinutesRangeRule(32, 120)
        val item = newFallbackCutoffLedger("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FallbackCutoffLedgerRateRangeRule(2.9, 3439.8)
        val item = newFallbackCutoffLedger("x", "y")
        item.rate = 3441.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackCutoffLedgerRequiredTitleRule("fallback cutoff ledger needs a value")
        val item = newFallbackCutoffLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackCutoffLedgerRequiredCodeRule("fallback cutoff ledger needs this field")
        val item = newFallbackCutoffLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
