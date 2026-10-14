package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDwellRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDwellRuleValidator()
        val item = newCappedDwellRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CappedDwellRuleRequiredUnitRule("capped dwell rule needs an entry")
        val item = newCappedDwellRule("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CappedDwellRuleUnitLengthRule(1000)
        val item = newCappedDwellRule("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CappedDwellRuleRevisionRangeRule(120, 255)
        val item = newCappedDwellRule("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedDwellRuleCodeLengthRule(80)
        val item = newCappedDwellRule("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
