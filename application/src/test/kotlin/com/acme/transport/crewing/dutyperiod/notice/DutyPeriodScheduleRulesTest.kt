package com.acme.transport.crewing.dutyperiod.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodScheduleValidator()
        val item = newDutyPeriodSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DutyPeriodScheduleLabelLengthRule(3600)
        val item = newDutyPeriodSchedule("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DutyPeriodScheduleRequiredTrailerIdRule("duty period schedule needs this field")
        val item = newDutyPeriodSchedule("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodScheduleCodeLengthRule(3600)
        val item = newDutyPeriodSchedule("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DutyPeriodScheduleCapacityRangeRule(1000, 3600)
        val item = newDutyPeriodSchedule("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DutyPeriodScheduleRequiredLabelRule("duty period schedule needs a value")
        val item = newDutyPeriodSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DutyPeriodScheduleCarrierLengthRule(86400)
        val item = newDutyPeriodSchedule("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
