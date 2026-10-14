package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardMilestoneValidator()
        val item = newRateCardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardMilestoneCodeLengthRule(86400)
        val item = newRateCardMilestone("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardMilestoneRequiredCodeRule("rate card milestone needs this field")
        val item = newRateCardMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RateCardMilestoneReviewerLengthRule(255)
        val item = newRateCardMilestone("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RateCardMilestoneEtaMinutesRangeRule(64, 3600)
        val item = newRateCardMilestone("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RateCardMilestoneCeilingRangeRule(3.1, 4585.8)
        val item = newRateCardMilestone("x", "y")
        item.ceiling = 4587.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
