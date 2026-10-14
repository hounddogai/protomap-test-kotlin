package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerAllocationValidator()
        val item = newLockerAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LockerAllocationTimezoneLengthRule(1000)
        val item = newLockerAllocation("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LockerAllocationCreatedAtRangeRule(64, 64)
        val item = newLockerAllocation("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LockerAllocationPriorityRangeRule(1000, 1000)
        val item = newLockerAllocation("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
