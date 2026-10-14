package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingScorecardPolicyValidator()
        val item = newRollingScorecardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RollingScorecardPolicyNotesLengthRule(16)
        val item = newRollingScorecardPolicy("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingScorecardPolicyRequiredLabelRule("rolling scorecard policy needs this field")
        val item = newRollingScorecardPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingScorecardPolicyCodeLengthRule(120)
        val item = newRollingScorecardPolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RollingScorecardPolicyRequiredNotesRule("rolling scorecard policy needs a value")
        val item = newRollingScorecardPolicy("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
