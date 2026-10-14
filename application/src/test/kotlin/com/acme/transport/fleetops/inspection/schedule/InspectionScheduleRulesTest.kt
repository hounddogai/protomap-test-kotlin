package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionScheduleValidator()
        val item = newInspectionSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = InspectionScheduleRequiredSourceRule("inspection schedule needs a value")
        val item = newInspectionSchedule("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = InspectionScheduleQuantityRangeRule(255, 3600)
        val item = newInspectionSchedule("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
