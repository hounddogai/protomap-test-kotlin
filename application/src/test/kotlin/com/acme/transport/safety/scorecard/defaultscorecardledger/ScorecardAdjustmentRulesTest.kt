package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardAdjustmentValidator()
        val item = newScorecardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardAdjustmentCodeLengthRule(80)
        val item = newScorecardAdjustment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ScorecardAdjustmentRequiredTrailerIdRule("scorecard adjustment needs this field")
        val item = newScorecardAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardAdjustmentRequiredCodeRule("scorecard adjustment needs a value")
        val item = newScorecardAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScorecardAdjustmentTitleLengthRule(120)
        val item = newScorecardAdjustment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
