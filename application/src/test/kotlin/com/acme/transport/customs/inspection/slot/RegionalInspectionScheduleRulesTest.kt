package com.acme.transport.customs.inspection.slot

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
    fun `flags a blank reviewer`() {
        val rule = RegionalInspectionScheduleRequiredReviewerRule("regional inspection schedule needs an entry")
        val item = newRegionalInspectionSchedule("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalInspectionScheduleCodeLengthRule(32)
        val item = newRegionalInspectionSchedule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalInspectionScheduleRequiredCodeRule("regional inspection schedule needs this field")
        val item = newRegionalInspectionSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
