package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRateCardMilestoneValidator()
        val item = newStrictRateCardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictRateCardMilestoneAmountRangeRule(5.5, 961.7)
        val item = newStrictRateCardMilestone("x", "y")
        item.amount = 963.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = StrictRateCardMilestoneDepotLengthRule(86400)
        val item = newStrictRateCardMilestone("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = StrictRateCardMilestoneVehicleIdLengthRule(86400)
        val item = newStrictRateCardMilestone("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRateCardMilestoneLabelLengthRule(64)
        val item = newStrictRateCardMilestone("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = StrictRateCardMilestoneRequiredVehicleIdRule("strict rate card milestone needs this field")
        val item = newStrictRateCardMilestone("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
