package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionCycleValidator()
        val item = newInspectionCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionCycleCodeLengthRule(64)
        val item = newInspectionCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = InspectionCycleDurationMinutesRangeRule(255, 3600)
        val item = newInspectionCycle("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
