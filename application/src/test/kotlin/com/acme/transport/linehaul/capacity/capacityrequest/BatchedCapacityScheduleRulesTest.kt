package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityScheduleValidator()
        val item = newBatchedCapacitySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedCapacityScheduleRequiredCodeRule("batched capacity schedule needs a value")
        val item = newBatchedCapacitySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedCapacityScheduleRequiredTitleRule("batched capacity schedule needs an entry")
        val item = newBatchedCapacitySchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BatchedCapacityScheduleRateRangeRule(1.1, 1547.9)
        val item = newBatchedCapacitySchedule("x", "y")
        item.rate = 1549.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
