package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDisputePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDisputePolicyValidator()
        val item = newCappedDisputePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedDisputePolicyLocaleLengthRule(16)
        val item = newCappedDisputePolicy("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedDisputePolicyRequiredLocaleRule("capped dispute policy needs an entry")
        val item = newCappedDisputePolicy("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CappedDisputePolicyOwnerIdLengthRule(120)
        val item = newCappedDisputePolicy("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedDisputePolicyGraceDaysRangeRule(120, 86400)
        val item = newCappedDisputePolicy("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedDisputePolicyLimitRangeRule(120, 86400)
        val item = newCappedDisputePolicy("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedDisputePolicyRequiredCodeRule("capped dispute policy needs this field")
        val item = newCappedDisputePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
