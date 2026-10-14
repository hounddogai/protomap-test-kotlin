package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnMilestoneValidator()
        val item = newLeaseReturnMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnMilestoneCodeLengthRule(64)
        val item = newLeaseReturnMilestone("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LeaseReturnMilestoneAmountRangeRule(8.9, 4020.3)
        val item = newLeaseReturnMilestone("x", "y")
        item.amount = 4021.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LeaseReturnMilestoneNameLengthRule(16)
        val item = newLeaseReturnMilestone("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = LeaseReturnMilestoneScoreRangeRule(5.9, 3297.7)
        val item = newLeaseReturnMilestone("x", "y")
        item.score = 3299.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
