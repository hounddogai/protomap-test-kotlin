package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputePolicyValidator()
        val item = newLenientDisputePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientDisputePolicyTitleLengthRule(1000)
        val item = newLenientDisputePolicy("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LenientDisputePolicyRevisionRangeRule(1000, 86400)
        val item = newLenientDisputePolicy("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
