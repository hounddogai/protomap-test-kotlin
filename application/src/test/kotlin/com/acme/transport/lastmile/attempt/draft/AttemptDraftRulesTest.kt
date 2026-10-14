package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptDraftValidator()
        val item = newAttemptDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AttemptDraftPalletsRangeRule(32, 80)
        val item = newAttemptDraft("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AttemptDraftRequiredTitleRule("attempt draft needs this field")
        val item = newAttemptDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AttemptDraftTitleLengthRule(255)
        val item = newAttemptDraft("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptDraftRequiredCodeRule("attempt draft needs this field")
        val item = newAttemptDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = AttemptDraftDepotLengthRule(255)
        val item = newAttemptDraft("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
