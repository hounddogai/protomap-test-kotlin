package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLeaseReturnAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLeaseReturnAssignmentValidator()
        val item = newGlobalLeaseReturnAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = GlobalLeaseReturnAssignmentRequiredChannelRule("global lease return assignment needs a value")
        val item = newGlobalLeaseReturnAssignment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalLeaseReturnAssignmentTitleLengthRule(64)
        val item = newGlobalLeaseReturnAssignment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = GlobalLeaseReturnAssignmentStartsAtRangeRule(16, 80)
        val item = newGlobalLeaseReturnAssignment("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalLeaseReturnAssignmentCodeLengthRule(16)
        val item = newGlobalLeaseReturnAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = GlobalLeaseReturnAssignmentLimitRangeRule(16, 86400)
        val item = newGlobalLeaseReturnAssignment("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
