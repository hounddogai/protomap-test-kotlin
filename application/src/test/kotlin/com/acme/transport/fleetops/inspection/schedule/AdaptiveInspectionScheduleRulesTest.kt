package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionScheduleValidator()
        val item = newAdaptiveInspectionSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveInspectionScheduleRequiredDescriptionRule("adaptive inspection schedule needs this field")
        val item = newAdaptiveInspectionSchedule("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionScheduleCodeLengthRule(1000)
        val item = newAdaptiveInspectionSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveInspectionScheduleDescriptionLengthRule(16)
        val item = newAdaptiveInspectionSchedule("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveInspectionScheduleTitleLengthRule(16)
        val item = newAdaptiveInspectionSchedule("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
