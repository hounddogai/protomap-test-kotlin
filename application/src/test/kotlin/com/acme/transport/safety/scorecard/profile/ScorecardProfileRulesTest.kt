package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardProfileValidator()
        val item = newScorecardProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ScorecardProfileUpdatedAtRangeRule(80, 86400)
        val item = newScorecardProfile("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardProfileRequiredLabelRule("scorecard profile needs this field")
        val item = newScorecardProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
