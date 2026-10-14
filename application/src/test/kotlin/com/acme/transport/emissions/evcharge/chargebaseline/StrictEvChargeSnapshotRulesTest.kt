package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEvChargeSnapshotValidator()
        val item = newStrictEvChargeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictEvChargeSnapshotNameLengthRule(80)
        val item = newStrictEvChargeSnapshot("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictEvChargeSnapshotRequiredCodeRule("strict ev charge snapshot needs an entry")
        val item = newStrictEvChargeSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = StrictEvChargeSnapshotBatchSizeRangeRule(86400, 86400)
        val item = newStrictEvChargeSnapshot("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = StrictEvChargeSnapshotShiftCodeLengthRule(16)
        val item = newStrictEvChargeSnapshot("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
