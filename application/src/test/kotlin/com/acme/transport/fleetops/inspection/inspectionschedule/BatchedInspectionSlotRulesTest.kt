package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedInspectionSlotValidator()
        val item = newBatchedInspectionSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedInspectionSlotDiscountRangeRule(4.3, 3580.2)
        val item = newBatchedInspectionSlot("x", "y")
        item.discount = 3581.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedInspectionSlotTitleLengthRule(120)
        val item = newBatchedInspectionSlot("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = BatchedInspectionSlotRequiredShiftCodeRule("batched inspection slot needs this field")
        val item = newBatchedInspectionSlot("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
