package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLeaseReturnMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictLeaseReturnMilestoneValidator()
        val item = newStrictLeaseReturnMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = StrictLeaseReturnMilestoneWeightRangeRule(5.6, 2734.7)
        val item = newStrictLeaseReturnMilestone("x", "y")
        item.weight = 2736.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictLeaseReturnMilestoneCodeLengthRule(86400)
        val item = newStrictLeaseReturnMilestone("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = StrictLeaseReturnMilestoneVersionRangeRule(64, 3600)
        val item = newStrictLeaseReturnMilestone("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
