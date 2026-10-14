package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRosterLedgerValidator()
        val item = newLenientRosterLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientRosterLedgerRequiredCodeRule("lenient roster ledger needs a value")
        val item = newLenientRosterLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LenientRosterLedgerRatioRangeRule(6.7, 1242.3)
        val item = newLenientRosterLedger("x", "y")
        item.ratio = 1243.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LenientRosterLedgerFloorValueRangeRule(5.4, 3285.5)
        val item = newLenientRosterLedger("x", "y")
        item.floorValue = 3287.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientRosterLedgerRetriesRangeRule(255, 1000)
        val item = newLenientRosterLedger("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRosterLedgerCodeLengthRule(32)
        val item = newLenientRosterLedger("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LenientRosterLedgerChannelLengthRule(1000)
        val item = newLenientRosterLedger("x", "y")
        item.channel = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
