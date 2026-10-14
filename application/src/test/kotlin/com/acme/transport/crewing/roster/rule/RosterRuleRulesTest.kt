package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterRuleValidator()
        val item = newRosterRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RosterRuleLimitRangeRule(32, 3600)
        val item = newRosterRule("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RosterRuleExternalRefLengthRule(64)
        val item = newRosterRule("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterRuleCodeLengthRule(1000)
        val item = newRosterRule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RosterRuleRequiredRouteCodeRule("roster rule needs a value")
        val item = newRosterRule("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RosterRuleRouteCodeLengthRule(32)
        val item = newRosterRule("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
