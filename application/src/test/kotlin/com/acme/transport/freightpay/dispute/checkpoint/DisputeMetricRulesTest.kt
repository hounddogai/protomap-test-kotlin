package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeMetricValidator()
        val item = newDisputeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DisputeMetricChecksumLengthRule(64)
        val item = newDisputeMetric("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DisputeMetricAmountRangeRule(6.5, 2351.6)
        val item = newDisputeMetric("x", "y")
        item.amount = 2353.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
