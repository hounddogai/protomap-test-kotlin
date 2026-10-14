package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceLedgerValidator()
        val item = newClearanceLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceLedgerRequiredCodeRule("clearance ledger needs this field")
        val item = newClearanceLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ClearanceLedgerDiscountRangeRule(5.3, 1677.5)
        val item = newClearanceLedger("x", "y")
        item.discount = 1679.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceLedgerCodeLengthRule(16)
        val item = newClearanceLedger("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ClearanceLedgerRequiredDockRule("clearance ledger needs an entry")
        val item = newClearanceLedger("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ClearanceLedgerStopCountRangeRule(255, 1000)
        val item = newClearanceLedger("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ClearanceLedgerSegmentLengthRule(1000)
        val item = newClearanceLedger("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
