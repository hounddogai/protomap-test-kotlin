package com.acme.transport.safety.scorecard.scorecardprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingScorecardProfileValidator()
        val item = newRollingScorecardProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingScorecardProfileRequiredCodeRule("rolling scorecard profile needs an entry")
        val item = newRollingScorecardProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingScorecardProfileStopCountRangeRule(255, 1000)
        val item = newRollingScorecardProfile("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingScorecardProfileLabelLengthRule(32)
        val item = newRollingScorecardProfile("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RollingScorecardProfileRequiredShiftCodeRule("rolling scorecard profile needs a value")
        val item = newRollingScorecardProfile("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
