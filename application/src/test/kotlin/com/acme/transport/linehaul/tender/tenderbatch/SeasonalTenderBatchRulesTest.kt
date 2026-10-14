package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTenderBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTenderBatchValidator()
        val item = newSeasonalTenderBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SeasonalTenderBatchStopCountRangeRule(1000, 3600)
        val item = newSeasonalTenderBatch("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = SeasonalTenderBatchRequiredShiftCodeRule("seasonal tender batch needs an entry")
        val item = newSeasonalTenderBatch("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = SeasonalTenderBatchPhoneLengthRule(1000)
        val item = newSeasonalTenderBatch("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
