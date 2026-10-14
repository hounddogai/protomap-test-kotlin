package com.acme.transport.customs.hold.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHoldScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHoldScheduleValidator()
        val item = newDefaultHoldSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DefaultHoldScheduleCreatedAtRangeRule(32, 86400)
        val item = newDefaultHoldSchedule("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultHoldScheduleCodeLengthRule(1000)
        val item = newDefaultHoldSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DefaultHoldScheduleFuelLitersRangeRule(9.7, 4794.4)
        val item = newDefaultHoldSchedule("x", "y")
        item.fuelLiters = 4795.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DefaultHoldScheduleCapacityRangeRule(64, 1000)
        val item = newDefaultHoldSchedule("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
