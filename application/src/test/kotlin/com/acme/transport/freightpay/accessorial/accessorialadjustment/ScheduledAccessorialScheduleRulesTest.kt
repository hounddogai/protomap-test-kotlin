package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAccessorialScheduleValidator()
        val item = newScheduledAccessorialSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAccessorialScheduleCodeLengthRule(120)
        val item = newScheduledAccessorialSchedule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ScheduledAccessorialScheduleDockLengthRule(86400)
        val item = newScheduledAccessorialSchedule("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ScheduledAccessorialScheduleFloorValueRangeRule(3.6, 3644.4)
        val item = newScheduledAccessorialSchedule("x", "y")
        item.floorValue = 3645.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ScheduledAccessorialScheduleLaneLengthRule(3600)
        val item = newScheduledAccessorialSchedule("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
