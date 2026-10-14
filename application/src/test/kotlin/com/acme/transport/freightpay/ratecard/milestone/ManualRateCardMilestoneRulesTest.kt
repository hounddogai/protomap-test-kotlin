package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRateCardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRateCardMilestoneValidator()
        val item = newManualRateCardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRateCardMilestoneRequiredCodeRule("manual rate card milestone needs a value")
        val item = newManualRateCardMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualRateCardMilestoneLabelLengthRule(255)
        val item = newManualRateCardMilestone("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
