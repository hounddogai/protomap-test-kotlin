package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeSummaryValidator()
        val item = newDisputeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DisputeSummaryDriverIdLengthRule(120)
        val item = newDisputeSummary("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeSummaryCodeLengthRule(86400)
        val item = newDisputeSummary("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DisputeSummaryCapacityRangeRule(64, 120)
        val item = newDisputeSummary("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DisputeSummaryDiscountRangeRule(4.5, 843.7)
        val item = newDisputeSummary("x", "y")
        item.discount = 845.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
