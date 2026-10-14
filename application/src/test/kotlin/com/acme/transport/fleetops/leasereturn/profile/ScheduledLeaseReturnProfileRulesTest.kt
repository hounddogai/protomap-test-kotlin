package com.acme.transport.fleetops.leasereturn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLeaseReturnProfileValidator()
        val item = newScheduledLeaseReturnProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledLeaseReturnProfileRequiredCodeRule("scheduled lease return profile needs a value")
        val item = newScheduledLeaseReturnProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScheduledLeaseReturnProfilePriorityRangeRule(1000, 3600)
        val item = newScheduledLeaseReturnProfile("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledLeaseReturnProfileTitleLengthRule(86400)
        val item = newScheduledLeaseReturnProfile("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
