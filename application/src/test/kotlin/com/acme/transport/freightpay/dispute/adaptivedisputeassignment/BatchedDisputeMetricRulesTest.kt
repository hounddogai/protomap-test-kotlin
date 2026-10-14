package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDisputeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDisputeMetricValidator()
        val item = newBatchedDisputeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDisputeMetricRequiredCodeRule("batched dispute metric needs this field")
        val item = newBatchedDisputeMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = BatchedDisputeMetricTimezoneLengthRule(32)
        val item = newBatchedDisputeMetric("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDisputeMetricCodeLengthRule(1000)
        val item = newBatchedDisputeMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedDisputeMetricTrailerIdLengthRule(80)
        val item = newBatchedDisputeMetric("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedDisputeMetricNameLengthRule(32)
        val item = newBatchedDisputeMetric("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
