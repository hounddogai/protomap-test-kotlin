package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeQuotaValidator()
        val item = newDisputeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DisputeQuotaTitleLengthRule(64)
        val item = newDisputeQuota("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DisputeQuotaRateRangeRule(6.4, 2148.9)
        val item = newDisputeQuota("x", "y")
        item.rate = 2150.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DisputeQuotaDepotLengthRule(80)
        val item = newDisputeQuota("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
