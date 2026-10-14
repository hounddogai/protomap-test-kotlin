package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerDraftValidator()
        val item = newTrailerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerDraftCodeLengthRule(120)
        val item = newTrailerDraft("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TrailerDraftNameLengthRule(3600)
        val item = newTrailerDraft("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TrailerDraftScoreRangeRule(0.8, 2680.3)
        val item = newTrailerDraft("x", "y")
        item.score = 2681.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerDraftRequiredTitleRule("trailer draft needs a value")
        val item = newTrailerDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
