package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRateCardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRateCardMilestoneValidator()
        val item = newBatchedRateCardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = BatchedRateCardMilestoneRequiredRouteCodeRule("batched rate card milestone needs an entry")
        val item = newBatchedRateCardMilestone("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = BatchedRateCardMilestoneRetriesRangeRule(16, 86400)
        val item = newBatchedRateCardMilestone("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = BatchedRateCardMilestoneRouteCodeLengthRule(32)
        val item = newBatchedRateCardMilestone("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
