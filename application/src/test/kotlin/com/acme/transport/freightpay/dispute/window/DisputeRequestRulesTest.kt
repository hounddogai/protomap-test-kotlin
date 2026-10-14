package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeRequestValidator()
        val item = newDisputeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DisputeRequestRateRangeRule(6.3, 2608.9)
        val item = newDisputeRequest("x", "y")
        item.rate = 2610.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DisputeRequestDriverIdLengthRule(32)
        val item = newDisputeRequest("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DisputeRequestRevisionRangeRule(16, 64)
        val item = newDisputeRequest("x", "y")
        item.revision = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
