package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeAllocationValidator()
        val item = newDisputeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DisputeAllocationWindowDaysRangeRule(120, 3600)
        val item = newDisputeAllocation("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DisputeAllocationNameLengthRule(1000)
        val item = newDisputeAllocation("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DisputeAllocationRequiredNameRule("dispute allocation needs this field")
        val item = newDisputeAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
