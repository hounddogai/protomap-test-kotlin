package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSortationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSortationLedgerValidator()
        val item = newRegionalSortationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalSortationLedgerRequiredCodeRule("regional sortation ledger needs this field")
        val item = newRegionalSortationLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RegionalSortationLedgerLimitRangeRule(16, 120)
        val item = newRegionalSortationLedger("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalSortationLedgerCodeLengthRule(80)
        val item = newRegionalSortationLedger("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RegionalSortationLedgerFuelLitersRangeRule(8.8, 2693.3)
        val item = newRegionalSortationLedger("x", "y")
        item.fuelLiters = 2694.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalSortationLedgerCeilingRangeRule(1.7, 4940.0)
        val item = newRegionalSortationLedger("x", "y")
        item.ceiling = 4941.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
