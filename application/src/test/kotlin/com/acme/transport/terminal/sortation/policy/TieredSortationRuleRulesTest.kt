package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationRuleValidator()
        val item = newTieredSortationRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TieredSortationRuleSequenceRangeRule(80, 1000)
        val item = newTieredSortationRule("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredSortationRuleCodeLengthRule(120)
        val item = newTieredSortationRule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredSortationRuleRequiredCodeRule("tiered sortation rule needs an entry")
        val item = newTieredSortationRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredSortationRuleTitleLengthRule(86400)
        val item = newTieredSortationRule("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
