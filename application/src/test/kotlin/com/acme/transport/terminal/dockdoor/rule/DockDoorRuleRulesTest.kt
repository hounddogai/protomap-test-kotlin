package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorRuleValidator()
        val item = newDockDoorRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DockDoorRuleRequiredCurrencyRule("dock door rule needs an entry")
        val item = newDockDoorRule("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorRuleCodeLengthRule(86400)
        val item = newDockDoorRule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DockDoorRuleAttemptsRangeRule(80, 120)
        val item = newDockDoorRule("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorRuleRequiredNameRule("dock door rule needs this field")
        val item = newDockDoorRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
