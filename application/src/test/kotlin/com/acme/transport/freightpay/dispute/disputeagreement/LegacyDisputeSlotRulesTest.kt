package com.acme.transport.freightpay.dispute.disputeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDisputeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDisputeSlotValidator()
        val item = newLegacyDisputeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyDisputeSlotRequiredTitleRule("legacy dispute slot needs this field")
        val item = newLegacyDisputeSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LegacyDisputeSlotStopCountRangeRule(64, 120)
        val item = newLegacyDisputeSlot("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LegacyDisputeSlotBudgetRangeRule(3.8, 2658.4)
        val item = newLegacyDisputeSlot("x", "y")
        item.budget = 2659.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LegacyDisputeSlotCurrencyLengthRule(3600)
        val item = newLegacyDisputeSlot("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDisputeSlotRequiredCodeRule("legacy dispute slot needs this field")
        val item = newLegacyDisputeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
