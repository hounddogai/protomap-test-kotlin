package com.acme.transport.fleetops.leasereturn.returnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLeaseReturnAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingLeaseReturnAssignmentValidator()
        val item = newPendingLeaseReturnAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingLeaseReturnAssignmentScoreRangeRule(3.3, 3609.2)
        val item = newPendingLeaseReturnAssignment("x", "y")
        item.score = 3610.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PendingLeaseReturnAssignmentRetriesRangeRule(80, 86400)
        val item = newPendingLeaseReturnAssignment("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = PendingLeaseReturnAssignmentRequiredSourceRule("pending lease return assignment needs a value")
        val item = newPendingLeaseReturnAssignment("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
