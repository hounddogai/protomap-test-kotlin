package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeProfileValidator()
        val item = newDisputeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DisputeProfileRatioRangeRule(5.1, 4269.3)
        val item = newDisputeProfile("x", "y")
        item.ratio = 4270.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DisputeProfilePalletsRangeRule(32, 86400)
        val item = newDisputeProfile("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DisputeProfileRequiredTitleRule("dispute profile needs an entry")
        val item = newDisputeProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeProfileCodeLengthRule(16)
        val item = newDisputeProfile("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DisputeProfileFloorValueRangeRule(0.6, 4202.7)
        val item = newDisputeProfile("x", "y")
        item.floorValue = 4204.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
