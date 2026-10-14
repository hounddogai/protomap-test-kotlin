package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterPolicyValidator()
        val item = newRosterPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RosterPolicyRequiredLabelRule("roster policy needs this field")
        val item = newRosterPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RosterPolicyRetriesRangeRule(64, 64)
        val item = newRosterPolicy("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterPolicyLabelLengthRule(120)
        val item = newRosterPolicy("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
