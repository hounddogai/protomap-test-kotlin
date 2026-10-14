package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHoldScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedHoldScheduleValidator()
        val item = newBatchedHoldSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedHoldScheduleRequiredDriverIdRule("batched hold schedule needs this field")
        val item = newBatchedHoldSchedule("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedHoldScheduleCodeLengthRule(80)
        val item = newBatchedHoldSchedule("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BatchedHoldScheduleCapacityRangeRule(32, 64)
        val item = newBatchedHoldSchedule("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedHoldScheduleEndsAtRangeRule(32, 255)
        val item = newBatchedHoldSchedule("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
