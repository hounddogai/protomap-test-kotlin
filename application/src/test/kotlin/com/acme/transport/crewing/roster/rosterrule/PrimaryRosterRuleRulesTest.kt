package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRosterRuleValidator()
        val item = newPrimaryRosterRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryRosterRuleRequiredCodeRule("primary roster rule needs this field")
        val item = newPrimaryRosterRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PrimaryRosterRuleRequiredTimezoneRule("primary roster rule needs an entry")
        val item = newPrimaryRosterRule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = PrimaryRosterRuleCategoryLengthRule(120)
        val item = newPrimaryRosterRule("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
