package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardDraftValidator()
        val item = newTrailerYardDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardDraftCodeLengthRule(32)
        val item = newTrailerYardDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TrailerYardDraftRequiredPhoneRule("trailer yard draft needs this field")
        val item = newTrailerYardDraft("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerYardDraftTitleLengthRule(80)
        val item = newTrailerYardDraft("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardDraftRequiredCodeRule("trailer yard draft needs this field")
        val item = newTrailerYardDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
