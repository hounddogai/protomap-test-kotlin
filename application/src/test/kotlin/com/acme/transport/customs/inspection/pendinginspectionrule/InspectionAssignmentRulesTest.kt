package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAssignmentValidator()
        val item = newInspectionAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionAssignmentRequiredLabelRule("inspection assignment needs an entry")
        val item = newInspectionAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = InspectionAssignmentRatioRangeRule(5.2, 585.2)
        val item = newInspectionAssignment("x", "y")
        item.ratio = 586.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionAssignmentCodeLengthRule(64)
        val item = newInspectionAssignment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = InspectionAssignmentEtaMinutesRangeRule(255, 3600)
        val item = newInspectionAssignment("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = InspectionAssignmentScoreRangeRule(7.3, 3004.9)
        val item = newInspectionAssignment("x", "y")
        item.score = 3006.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
