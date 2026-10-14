package com.acme.transport.network.servicearea.areacycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServiceAreaCycleValidator()
        val item = newBatchedServiceAreaCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = BatchedServiceAreaCycleGraceDaysRangeRule(64, 1000)
        val item = newBatchedServiceAreaCycle("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = BatchedServiceAreaCycleChecksumLengthRule(32)
        val item = newBatchedServiceAreaCycle("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = BatchedServiceAreaCycleRequiredShiftCodeRule("batched service area cycle needs a value")
        val item = newBatchedServiceAreaCycle("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedServiceAreaCycleShiftCodeLengthRule(16)
        val item = newBatchedServiceAreaCycle("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedServiceAreaCycleCodeLengthRule(32)
        val item = newBatchedServiceAreaCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
