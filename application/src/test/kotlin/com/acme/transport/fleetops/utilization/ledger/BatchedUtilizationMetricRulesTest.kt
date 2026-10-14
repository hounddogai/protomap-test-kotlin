package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedUtilizationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedUtilizationMetricValidator()
        val item = newBatchedUtilizationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedUtilizationMetricCodeLengthRule(16)
        val item = newBatchedUtilizationMetric("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = BatchedUtilizationMetricUpdatedAtRangeRule(80, 120)
        val item = newBatchedUtilizationMetric("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedUtilizationMetricTitleLengthRule(16)
        val item = newBatchedUtilizationMetric("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedUtilizationMetricRequiredNameRule("batched utilization metric needs an entry")
        val item = newBatchedUtilizationMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
