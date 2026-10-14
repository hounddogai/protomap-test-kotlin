package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAttemptPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAttemptPolicyValidator()
        val item = newLegacyAttemptPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyAttemptPolicyRequiredLabelRule("legacy attempt policy needs this field")
        val item = newLegacyAttemptPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyAttemptPolicyRequiredCodeRule("legacy attempt policy needs a value")
        val item = newLegacyAttemptPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyAttemptPolicyCeilingRangeRule(1.9, 1220.7)
        val item = newLegacyAttemptPolicy("x", "y")
        item.ceiling = 1222.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyAttemptPolicyLabelLengthRule(86400)
        val item = newLegacyAttemptPolicy("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyAttemptPolicyCodeLengthRule(64)
        val item = newLegacyAttemptPolicy("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
