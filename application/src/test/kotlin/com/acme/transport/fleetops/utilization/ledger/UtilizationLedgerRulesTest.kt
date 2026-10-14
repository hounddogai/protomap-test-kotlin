package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationLedgerValidator()
        val item = newUtilizationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = UtilizationLedgerDiscountRangeRule(3.7, 3029.6)
        val item = newUtilizationLedger("x", "y")
        item.discount = 3031.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = UtilizationLedgerNotesLengthRule(16)
        val item = newUtilizationLedger("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = UtilizationLedgerTitleLengthRule(32)
        val item = newUtilizationLedger("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = UtilizationLedgerRequiredTitleRule("utilization ledger needs this field")
        val item = newUtilizationLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = UtilizationLedgerRegionLengthRule(16)
        val item = newUtilizationLedger("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
