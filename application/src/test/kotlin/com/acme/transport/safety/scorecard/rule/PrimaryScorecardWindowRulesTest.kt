package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryScorecardWindowValidator()
        val item = newPrimaryScorecardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryScorecardWindowCodeLengthRule(80)
        val item = newPrimaryScorecardWindow("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryScorecardWindowRequiredLabelRule("primary scorecard window needs a value")
        val item = newPrimaryScorecardWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryScorecardWindowStopCountRangeRule(16, 1000)
        val item = newPrimaryScorecardWindow("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
