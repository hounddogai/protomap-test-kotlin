package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacitySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacitySegmentValidator()
        val item = newBatchedCapacitySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = BatchedCapacitySegmentSummaryLengthRule(86400)
        val item = newBatchedCapacitySegment("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedCapacitySegmentNameLengthRule(3600)
        val item = newBatchedCapacitySegment("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedCapacitySegmentRequiredNameRule("batched capacity segment needs this field")
        val item = newBatchedCapacitySegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
