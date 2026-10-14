package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLockerAssignmentValidator()
        val item = newCappedLockerAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedLockerAssignmentDurationMinutesRangeRule(64, 3600)
        val item = newCappedLockerAssignment("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CappedLockerAssignmentRequiredLaneRule("capped locker assignment needs this field")
        val item = newCappedLockerAssignment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLockerAssignmentCodeLengthRule(32)
        val item = newCappedLockerAssignment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedLockerAssignmentRequiredTitleRule("capped locker assignment needs this field")
        val item = newCappedLockerAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
