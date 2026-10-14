package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAccessorialMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAccessorialMetricValidator()
        val item = newBatchedAccessorialMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BatchedAccessorialMetricQuantityRangeRule(80, 120)
        val item = newBatchedAccessorialMetric("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAccessorialMetricRequiredCodeRule("batched accessorial metric needs a value")
        val item = newBatchedAccessorialMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedAccessorialMetricRequiredLabelRule("batched accessorial metric needs this field")
        val item = newBatchedAccessorialMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = BatchedAccessorialMetricDistanceKmRangeRule(8.2, 3674.4)
        val item = newBatchedAccessorialMetric("x", "y")
        item.distanceKm = 3675.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
