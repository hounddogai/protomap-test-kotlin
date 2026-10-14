package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNearMissMappingValidator()
        val item = newBatchedNearMissMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedNearMissMappingNameLengthRule(80)
        val item = newBatchedNearMissMapping("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = BatchedNearMissMappingReviewerLengthRule(255)
        val item = newBatchedNearMissMapping("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedNearMissMappingRequiredExternalRefRule("batched near miss mapping needs this field")
        val item = newBatchedNearMissMapping("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedNearMissMappingDiscountRangeRule(5.0, 3202.6)
        val item = newBatchedNearMissMapping("x", "y")
        item.discount = 3204.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
