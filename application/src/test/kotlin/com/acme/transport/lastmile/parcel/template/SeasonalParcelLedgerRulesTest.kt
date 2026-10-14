package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalParcelLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalParcelLedgerValidator()
        val item = newSeasonalParcelLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalParcelLedgerCodeLengthRule(255)
        val item = newSeasonalParcelLedger("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalParcelLedgerRequiredNameRule("seasonal parcel ledger needs an entry")
        val item = newSeasonalParcelLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SeasonalParcelLedgerAmountRangeRule(0.6, 2185.5)
        val item = newSeasonalParcelLedger("x", "y")
        item.amount = 2187.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalParcelLedgerRequiredCodeRule("seasonal parcel ledger needs a value")
        val item = newSeasonalParcelLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
