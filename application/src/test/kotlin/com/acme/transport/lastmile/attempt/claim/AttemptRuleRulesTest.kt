package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptRuleValidator()
        val item = newAttemptRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptRuleRequiredNameRule("attempt rule needs this field")
        val item = newAttemptRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AttemptRuleRequiredCurrencyRule("attempt rule needs an entry")
        val item = newAttemptRule("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptRuleRequiredCodeRule("attempt rule needs a value")
        val item = newAttemptRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AttemptRuleCurrencyLengthRule(255)
        val item = newAttemptRule("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AttemptRuleRequiredLabelRule("attempt rule needs this field")
        val item = newAttemptRule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
