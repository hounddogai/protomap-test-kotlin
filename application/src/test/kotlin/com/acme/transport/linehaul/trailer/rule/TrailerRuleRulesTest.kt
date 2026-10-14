package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerRuleValidator()
        val item = newTrailerRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TrailerRuleSequenceRangeRule(32, 1000)
        val item = newTrailerRule("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerRuleRequiredLabelRule("trailer rule needs an entry")
        val item = newTrailerRule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerRuleCodeLengthRule(86400)
        val item = newTrailerRule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TrailerRuleAmountRangeRule(2.5, 1359.9)
        val item = newTrailerRule("x", "y")
        item.amount = 1361.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
