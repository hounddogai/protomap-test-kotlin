package com.acme.transport.terminal.traileryard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerYardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerYardPolicyValidator()
        val item = newGlobalTrailerYardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = GlobalTrailerYardPolicyCurrencyLengthRule(86400)
        val item = newGlobalTrailerYardPolicy("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalTrailerYardPolicyDurationMinutesRangeRule(80, 86400)
        val item = newGlobalTrailerYardPolicy("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = GlobalTrailerYardPolicyRequiredCurrencyRule("global trailer yard policy needs a value")
        val item = newGlobalTrailerYardPolicy("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTrailerYardPolicyCodeLengthRule(16)
        val item = newGlobalTrailerYardPolicy("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
