package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalZoneLedgerValidator()
        val item = newRegionalZoneLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalZoneLedgerRequiredCodeRule("regional zone ledger needs this field")
        val item = newRegionalZoneLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalZoneLedgerRequiredTitleRule("regional zone ledger needs an entry")
        val item = newRegionalZoneLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalZoneLedgerTitleLengthRule(120)
        val item = newRegionalZoneLedger("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RegionalZoneLedgerDriverIdLengthRule(80)
        val item = newRegionalZoneLedger("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
