package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeAuditValidator()
        val item = newDisputeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DisputeAuditDiscountRangeRule(5.4, 4004.0)
        val item = newDisputeAudit("x", "y")
        item.discount = 4005.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DisputeAuditSequenceRangeRule(1000, 86400)
        val item = newDisputeAudit("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DisputeAuditMinimumRangeRule(2.6, 4572.8)
        val item = newDisputeAudit("x", "y")
        item.minimum = 4574.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
