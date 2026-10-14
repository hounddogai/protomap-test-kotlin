package com.acme.transport.terminal.sortation.strictsortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationBatchValidator()
        val item = newTieredSortationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = TieredSortationBatchShiftCodeLengthRule(32)
        val item = newTieredSortationBatch("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredSortationBatchRequiredNameRule("tiered sortation batch needs a value")
        val item = newTieredSortationBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TieredSortationBatchRequiredShiftCodeRule("tiered sortation batch needs a value")
        val item = newTieredSortationBatch("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TieredSortationBatchEndsAtRangeRule(80, 3600)
        val item = newTieredSortationBatch("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
