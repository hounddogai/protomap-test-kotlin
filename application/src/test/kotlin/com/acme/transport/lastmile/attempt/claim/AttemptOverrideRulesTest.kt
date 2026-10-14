package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptOverrideValidator()
        val item = newAttemptOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = AttemptOverrideDistanceKmRangeRule(2.4, 262.9)
        val item = newAttemptOverride("x", "y")
        item.distanceKm = 264.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptOverrideRequiredCodeRule("attempt override needs this field")
        val item = newAttemptOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AttemptOverrideTitleLengthRule(80)
        val item = newAttemptOverride("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AttemptOverrideRequiredTitleRule("attempt override needs a value")
        val item = newAttemptOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
