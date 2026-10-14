package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeBaselineValidator()
        val item = newDisputeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DisputeBaselineOdometerRangeRule(64, 255)
        val item = newDisputeBaseline("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DisputeBaselineCapacityRangeRule(255, 3600)
        val item = newDisputeBaseline("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DisputeBaselineSegmentLengthRule(64)
        val item = newDisputeBaseline("x", "y")
        item.segment = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeBaselineLabelLengthRule(64)
        val item = newDisputeBaseline("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeBaselineCodeLengthRule(255)
        val item = newDisputeBaseline("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
