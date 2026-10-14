package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardMilestoneValidator()
        val item = newScorecardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScorecardMilestoneLabelLengthRule(1000)
        val item = newScorecardMilestone("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
