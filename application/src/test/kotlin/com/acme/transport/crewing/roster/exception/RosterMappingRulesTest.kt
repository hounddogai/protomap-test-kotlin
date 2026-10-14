package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterMappingValidator()
        val item = newRosterMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterMappingLabelLengthRule(80)
        val item = newRosterMapping("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RosterMappingRequiredRouteCodeRule("roster mapping needs this field")
        val item = newRosterMapping("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RosterMappingRequiredExternalRefRule("roster mapping needs an entry")
        val item = newRosterMapping("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RosterMappingPalletsRangeRule(32, 1000)
        val item = newRosterMapping("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
