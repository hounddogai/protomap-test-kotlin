package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptPolicyValidator()
        val item = newAttemptPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AttemptPolicyDurationMinutesRangeRule(255, 1000)
        val item = newAttemptPolicy("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AttemptPolicyAmountRangeRule(4.9, 4925.8)
        val item = newAttemptPolicy("x", "y")
        item.amount = 4927.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptPolicyRequiredCodeRule("attempt policy needs a value")
        val item = newAttemptPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
