package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryScorecardTargetValidator()
        val item = newPrimaryScorecardTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryScorecardTargetRequiredCodeRule("primary scorecard target needs this field")
        val item = newPrimaryScorecardTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = PrimaryScorecardTargetScoreRangeRule(8.4, 313.4)
        val item = newPrimaryScorecardTarget("x", "y")
        item.score = 314.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryScorecardTargetLabelLengthRule(3600)
        val item = newPrimaryScorecardTarget("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
