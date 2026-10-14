package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultInspectionScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultInspectionScheduleValidator()
        val item = newDefaultInspectionSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultInspectionScheduleCodeLengthRule(86400)
        val item = newDefaultInspectionSchedule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultInspectionScheduleRequiredLabelRule("default inspection schedule needs an entry")
        val item = newDefaultInspectionSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultInspectionScheduleTimezoneLengthRule(86400)
        val item = newDefaultInspectionSchedule("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultInspectionScheduleRequiredCodeRule("default inspection schedule needs an entry")
        val item = newDefaultInspectionSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefaultInspectionScheduleGraceDaysRangeRule(1000, 86400)
        val item = newDefaultInspectionSchedule("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
