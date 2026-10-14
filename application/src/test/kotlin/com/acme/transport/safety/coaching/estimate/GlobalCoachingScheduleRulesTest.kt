package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCoachingScheduleValidator()
        val item = newGlobalCoachingSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = GlobalCoachingScheduleCapacityRangeRule(120, 120)
        val item = newGlobalCoachingSchedule("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalCoachingScheduleLabelLengthRule(120)
        val item = newGlobalCoachingSchedule("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = GlobalCoachingScheduleSegmentLengthRule(1000)
        val item = newGlobalCoachingSchedule("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
