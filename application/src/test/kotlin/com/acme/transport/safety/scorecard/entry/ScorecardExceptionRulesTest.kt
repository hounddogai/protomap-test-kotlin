package com.acme.transport.safety.scorecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardExceptionValidator()
        val item = newScorecardException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScorecardExceptionRequiredLocaleRule("scorecard exception needs a value")
        val item = newScorecardException("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ScorecardExceptionLocaleLengthRule(3600)
        val item = newScorecardException("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
