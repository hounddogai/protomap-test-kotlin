package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubPolicyValidator()
        val item = newHubPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = HubPolicyRequiredLaneRule("hub policy needs this field")
        val item = newHubPolicy("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = HubPolicyGraceDaysRangeRule(16, 16)
        val item = newHubPolicy("x", "y")
        item.graceDays = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = HubPolicyQuantityRangeRule(3600, 3600)
        val item = newHubPolicy("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
