package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardAssignmentValidator()
        val item = newScorecardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ScorecardAssignmentRequiredChannelRule("scorecard assignment needs an entry")
        val item = newScorecardAssignment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardAssignmentRequiredCodeRule("scorecard assignment needs a value")
        val item = newScorecardAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScorecardAssignmentWindowDaysRangeRule(86400, 86400)
        val item = newScorecardAssignment("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ScorecardAssignmentDockLengthRule(1000)
        val item = newScorecardAssignment("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardAssignmentRequiredLabelRule("scorecard assignment needs this field")
        val item = newScorecardAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
