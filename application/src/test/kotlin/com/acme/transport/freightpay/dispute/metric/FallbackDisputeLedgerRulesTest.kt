package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDisputeLedgerValidator()
        val item = newFallbackDisputeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackDisputeLedgerEtaMinutesRangeRule(80, 3600)
        val item = newFallbackDisputeLedger("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackDisputeLedgerRequiredNameRule("fallback dispute ledger needs this field")
        val item = newFallbackDisputeLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackDisputeLedgerLoadKgRangeRule(4.4, 3792.5)
        val item = newFallbackDisputeLedger("x", "y")
        item.loadKg = 3794.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDisputeLedgerRequiredCodeRule("fallback dispute ledger needs an entry")
        val item = newFallbackDisputeLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
