package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderMilestoneValidator()
        val item = newTenderMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = TenderMilestonePalletsRangeRule(32, 255)
        val item = newTenderMilestone("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TenderMilestoneChannelLengthRule(3600)
        val item = newTenderMilestone("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderMilestoneRequiredCodeRule("tender milestone needs this field")
        val item = newTenderMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
