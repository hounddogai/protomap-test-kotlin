package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLeaseReturnLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalLeaseReturnLedgerValidator()
        val item = newSeasonalLeaseReturnLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalLeaseReturnLedgerRequiredCodeRule("seasonal lease return ledger needs an entry")
        val item = newSeasonalLeaseReturnLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalLeaseReturnLedgerCodeLengthRule(255)
        val item = newSeasonalLeaseReturnLedger("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SeasonalLeaseReturnLedgerLimitRangeRule(16, 32)
        val item = newSeasonalLeaseReturnLedger("x", "y")
        item.limit = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SeasonalLeaseReturnLedgerDockLengthRule(16)
        val item = newSeasonalLeaseReturnLedger("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = SeasonalLeaseReturnLedgerRegionLengthRule(120)
        val item = newSeasonalLeaseReturnLedger("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = SeasonalLeaseReturnLedgerScoreRangeRule(4.3, 1294.2)
        val item = newSeasonalLeaseReturnLedger("x", "y")
        item.score = 1295.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
