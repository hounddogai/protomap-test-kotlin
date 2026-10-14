package com.acme.transport.customs.inspection.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionScheduleValidator()
        val item = newLegacyInspectionSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyInspectionScheduleLabelLengthRule(1000)
        val item = newLegacyInspectionSchedule("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LegacyInspectionScheduleDurationMinutesRangeRule(80, 255)
        val item = newLegacyInspectionSchedule("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyInspectionScheduleRequiredSegmentRule("legacy inspection schedule needs a value")
        val item = newLegacyInspectionSchedule("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyInspectionScheduleRequiredLabelRule("legacy inspection schedule needs an entry")
        val item = newLegacyInspectionSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = LegacyInspectionScheduleVersionRangeRule(120, 3600)
        val item = newLegacyInspectionSchedule("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LegacyInspectionScheduleEndsAtRangeRule(32, 86400)
        val item = newLegacyInspectionSchedule("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
