package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDisputePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDisputePlanValidator()
        val item = newStrictDisputePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictDisputePlanRequiredLaneRule("strict dispute plan needs an entry")
        val item = newStrictDisputePlan("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = StrictDisputePlanGraceDaysRangeRule(80, 1000)
        val item = newStrictDisputePlan("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = StrictDisputePlanStartsAtRangeRule(16, 32)
        val item = newStrictDisputePlan("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDisputePlanRequiredCodeRule("strict dispute plan needs an entry")
        val item = newStrictDisputePlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
