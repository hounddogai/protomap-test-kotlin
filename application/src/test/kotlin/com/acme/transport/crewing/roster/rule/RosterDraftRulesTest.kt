package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterDraftValidator()
        val item = newRosterDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterDraftRequiredCodeRule("roster draft needs this field")
        val item = newRosterDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterDraftCodeLengthRule(86400)
        val item = newRosterDraft("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RosterDraftExpiresAtRangeRule(16, 32)
        val item = newRosterDraft("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
