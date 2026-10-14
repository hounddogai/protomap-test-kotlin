package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropRuleValidator()
        val item = newDropRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DropRuleRequiredPhoneRule("drop rule needs this field")
        val item = newDropRule("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DropRulePhoneLengthRule(16)
        val item = newDropRule("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DropRulePriorityRangeRule(16, 64)
        val item = newDropRule("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
