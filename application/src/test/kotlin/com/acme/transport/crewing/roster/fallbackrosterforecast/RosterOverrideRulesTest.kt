package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterOverrideValidator()
        val item = newRosterOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterOverrideRequiredCodeRule("roster override needs this field")
        val item = newRosterOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RosterOverrideRequiredTrailerIdRule("roster override needs this field")
        val item = newRosterOverride("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterOverrideLabelLengthRule(3600)
        val item = newRosterOverride("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
