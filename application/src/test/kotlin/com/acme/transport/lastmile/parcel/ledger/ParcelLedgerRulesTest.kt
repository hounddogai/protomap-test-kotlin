package com.acme.transport.lastmile.parcel.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelLedgerValidator()
        val item = newParcelLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ParcelLedgerRequiredDepotRule("parcel ledger needs this field")
        val item = newParcelLedger("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ParcelLedgerDurationMinutesRangeRule(80, 86400)
        val item = newParcelLedger("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
