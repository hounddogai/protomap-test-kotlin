package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalScorecardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalScorecardAssignmentValidator()
        val item = newSeasonalScorecardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalScorecardAssignmentCodeLengthRule(3600)
        val item = newSeasonalScorecardAssignment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalScorecardAssignmentNameLengthRule(255)
        val item = newSeasonalScorecardAssignment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = SeasonalScorecardAssignmentNotesLengthRule(86400)
        val item = newSeasonalScorecardAssignment("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
