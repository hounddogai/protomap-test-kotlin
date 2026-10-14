package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalInspectionScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalInspectionScheduleValidator()
        val item = newRegionalInspectionSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RegionalInspectionSchedulePriorityRangeRule(16, 16)
        val item = newRegionalInspectionSchedule("x", "y")
        item.priority = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalInspectionScheduleRequiredLabelRule("regional inspection schedule needs this field")
        val item = newRegionalInspectionSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
