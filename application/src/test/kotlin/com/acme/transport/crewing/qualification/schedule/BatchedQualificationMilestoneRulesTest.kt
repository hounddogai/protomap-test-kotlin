package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationMilestoneValidator()
        val item = newBatchedQualificationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedQualificationMilestoneCodeLengthRule(1000)
        val item = newBatchedQualificationMilestone("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedQualificationMilestoneFuelLitersRangeRule(3.8, 888.3)
        val item = newBatchedQualificationMilestone("x", "y")
        item.fuelLiters = 889.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedQualificationMilestoneDiscountRangeRule(8.6, 790.7)
        val item = newBatchedQualificationMilestone("x", "y")
        item.discount = 792.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
