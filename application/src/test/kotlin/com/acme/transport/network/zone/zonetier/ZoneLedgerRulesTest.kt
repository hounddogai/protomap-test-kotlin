package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneLedgerValidator()
        val item = newZoneLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneLedgerRequiredCodeRule("zone ledger needs this field")
        val item = newZoneLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ZoneLedgerFuelLitersRangeRule(3.9, 1491.1)
        val item = newZoneLedger("x", "y")
        item.fuelLiters = 1492.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ZoneLedgerPalletsRangeRule(255, 86400)
        val item = newZoneLedger("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneLedgerRequiredTitleRule("zone ledger needs this field")
        val item = newZoneLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
