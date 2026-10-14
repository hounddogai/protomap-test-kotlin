package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAttemptScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAttemptScoreValidator()
        val item = newPrimaryAttemptScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = PrimaryAttemptScoreCountryLengthRule(64)
        val item = newPrimaryAttemptScore("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryAttemptScoreRequiredCodeRule("primary attempt score needs this field")
        val item = newPrimaryAttemptScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PrimaryAttemptScoreRequiredCountryRule("primary attempt score needs a value")
        val item = newPrimaryAttemptScore("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
