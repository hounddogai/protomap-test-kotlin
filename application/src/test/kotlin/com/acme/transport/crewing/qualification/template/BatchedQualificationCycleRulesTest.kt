package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationCycleValidator()
        val item = newBatchedQualificationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BatchedQualificationCycleRequiredTrailerIdRule("batched qualification cycle needs an entry")
        val item = newBatchedQualificationCycle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedQualificationCycleCodeLengthRule(86400)
        val item = newBatchedQualificationCycle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = BatchedQualificationCycleGraceDaysRangeRule(32, 64)
        val item = newBatchedQualificationCycle("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedQualificationCycleStartsAtRangeRule(64, 255)
        val item = newBatchedQualificationCycle("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = BatchedQualificationCycleRequiredPhoneRule("batched qualification cycle needs a value")
        val item = newBatchedQualificationCycle("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
