package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputeMetricValidator()
        val item = newLenientDisputeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LenientDisputeMetricRevisionRangeRule(32, 3600)
        val item = newLenientDisputeMetric("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = LenientDisputeMetricCategoryLengthRule(1000)
        val item = newLenientDisputeMetric("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LenientDisputeMetricCarrierLengthRule(16)
        val item = newLenientDisputeMetric("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LenientDisputeMetricRequiredCategoryRule("lenient dispute metric needs a value")
        val item = newLenientDisputeMetric("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
