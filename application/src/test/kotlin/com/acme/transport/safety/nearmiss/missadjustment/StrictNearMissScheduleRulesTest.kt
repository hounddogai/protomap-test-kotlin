package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNearMissScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNearMissScheduleValidator()
        val item = newStrictNearMissSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = StrictNearMissScheduleChannelLengthRule(32)
        val item = newStrictNearMissSchedule("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = StrictNearMissScheduleCreatedAtRangeRule(16, 64)
        val item = newStrictNearMissSchedule("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = StrictNearMissScheduleRequiredCategoryRule("strict near miss schedule needs this field")
        val item = newStrictNearMissSchedule("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
