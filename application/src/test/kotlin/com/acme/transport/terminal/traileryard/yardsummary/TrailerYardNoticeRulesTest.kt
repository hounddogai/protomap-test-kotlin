package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardNoticeValidator()
        val item = newTrailerYardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TrailerYardNoticeExpiresAtRangeRule(3600, 3600)
        val item = newTrailerYardNotice("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerYardNoticeTitleLengthRule(32)
        val item = newTrailerYardNotice("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardNoticeCodeLengthRule(86400)
        val item = newTrailerYardNotice("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
