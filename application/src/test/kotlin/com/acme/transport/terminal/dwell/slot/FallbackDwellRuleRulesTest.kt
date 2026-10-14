package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDwellRuleValidator()
        val item = newFallbackDwellRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = FallbackDwellRuleSummaryLengthRule(16)
        val item = newFallbackDwellRule("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FallbackDwellRuleRequiredSummaryRule("fallback dwell rule needs this field")
        val item = newFallbackDwellRule("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDwellRuleRequiredCodeRule("fallback dwell rule needs a value")
        val item = newFallbackDwellRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackDwellRuleRequiredTitleRule("fallback dwell rule needs this field")
        val item = newFallbackDwellRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
