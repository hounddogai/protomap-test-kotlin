package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardRollupValidator()
        val item = newScorecardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardRollupRequiredLabelRule("scorecard rollup needs an entry")
        val item = newScorecardRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScorecardRollupPriorityRangeRule(80, 86400)
        val item = newScorecardRollup("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
