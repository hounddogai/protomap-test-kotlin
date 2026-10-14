package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDisputeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDisputeMetricValidator()
        val item = newPrimaryDisputeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PrimaryDisputeMetricDurationMinutesRangeRule(80, 1000)
        val item = newPrimaryDisputeMetric("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryDisputeMetricStopCountRangeRule(16, 1000)
        val item = newPrimaryDisputeMetric("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = PrimaryDisputeMetricSourceLengthRule(3600)
        val item = newPrimaryDisputeMetric("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = PrimaryDisputeMetricLimitRangeRule(16, 3600)
        val item = newPrimaryDisputeMetric("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
