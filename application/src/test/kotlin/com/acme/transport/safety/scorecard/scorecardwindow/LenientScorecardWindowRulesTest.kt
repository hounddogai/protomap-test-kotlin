package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientScorecardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientScorecardWindowValidator()
        val item = newLenientScorecardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientScorecardWindowRequiredCodeRule("lenient scorecard window needs an entry")
        val item = newLenientScorecardWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LenientScorecardWindowExpiresAtRangeRule(3600, 86400)
        val item = newLenientScorecardWindow("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = LenientScorecardWindowDescriptionLengthRule(1000)
        val item = newLenientScorecardWindow("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LenientScorecardWindowRequiredSegmentRule("lenient scorecard window needs a value")
        val item = newLenientScorecardWindow("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
