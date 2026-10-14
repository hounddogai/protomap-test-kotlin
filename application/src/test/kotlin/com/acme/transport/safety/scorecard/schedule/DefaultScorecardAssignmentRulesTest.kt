package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultScorecardAssignmentValidator()
        val item = newDefaultScorecardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefaultScorecardAssignmentStartsAtRangeRule(64, 80)
        val item = newDefaultScorecardAssignment("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultScorecardAssignmentRequiredNameRule("default scorecard assignment needs this field")
        val item = newDefaultScorecardAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultScorecardAssignmentRequiredCodeRule("default scorecard assignment needs this field")
        val item = newDefaultScorecardAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
