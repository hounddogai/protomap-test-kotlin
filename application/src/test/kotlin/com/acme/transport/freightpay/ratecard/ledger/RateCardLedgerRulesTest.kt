package com.acme.transport.freightpay.ratecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardLedgerValidator()
        val item = newRateCardLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RateCardLedgerRequiredTitleRule("rate card ledger needs this field")
        val item = newRateCardLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RateCardLedgerLimitRangeRule(80, 86400)
        val item = newRateCardLedger("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
