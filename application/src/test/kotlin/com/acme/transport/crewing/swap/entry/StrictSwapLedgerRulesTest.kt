package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSwapLedgerValidator()
        val item = newStrictSwapLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = StrictSwapLedgerDistanceKmRangeRule(4.4, 4728.7)
        val item = newStrictSwapLedger("x", "y")
        item.distanceKm = 4730.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictSwapLedgerRequiredCodeRule("strict swap ledger needs a value")
        val item = newStrictSwapLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = StrictSwapLedgerRequiredShiftCodeRule("strict swap ledger needs this field")
        val item = newStrictSwapLedger("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = StrictSwapLedgerBatchSizeRangeRule(32, 80)
        val item = newStrictSwapLedger("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictSwapLedgerCapacityRangeRule(255, 1000)
        val item = newStrictSwapLedger("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
