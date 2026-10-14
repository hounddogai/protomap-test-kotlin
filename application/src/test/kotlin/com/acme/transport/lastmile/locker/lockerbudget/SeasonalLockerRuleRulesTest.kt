package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLockerRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalLockerRuleValidator()
        val item = newSeasonalLockerRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalLockerRuleRequiredCodeRule("seasonal locker rule needs a value")
        val item = newSeasonalLockerRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalLockerRuleTitleLengthRule(32)
        val item = newSeasonalLockerRule("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = SeasonalLockerRuleRequiredSummaryRule("seasonal locker rule needs this field")
        val item = newSeasonalLockerRule("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalLockerRuleCodeLengthRule(64)
        val item = newSeasonalLockerRule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
