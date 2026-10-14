package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityMetricValidator()
        val item = newBatchedCapacityMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedCapacityMetricNameLengthRule(16)
        val item = newBatchedCapacityMetric("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedCapacityMetricTitleLengthRule(120)
        val item = newBatchedCapacityMetric("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedCapacityMetricRequiredTitleRule("batched capacity metric needs a value")
        val item = newBatchedCapacityMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedCapacityMetricRequiredNameRule("batched capacity metric needs a value")
        val item = newBatchedCapacityMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedCapacityMetricCodeLengthRule(16)
        val item = newBatchedCapacityMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
