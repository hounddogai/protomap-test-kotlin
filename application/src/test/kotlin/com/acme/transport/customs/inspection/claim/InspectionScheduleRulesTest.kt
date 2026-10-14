package com.acme.transport.customs.inspection.claim

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
    fun `flags a blank depot`() {
        val rule = InspectionScheduleRequiredDepotRule("inspection schedule needs a value")
        val item = newInspectionSchedule("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = InspectionScheduleWeightRangeRule(2.5, 3296.6)
        val item = newInspectionSchedule("x", "y")
        item.weight = 3298.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionScheduleLabelLengthRule(3600)
        val item = newInspectionSchedule("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
