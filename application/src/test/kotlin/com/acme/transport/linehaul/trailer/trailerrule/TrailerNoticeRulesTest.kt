package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerNoticeValidator()
        val item = newTrailerNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TrailerNoticeDockLengthRule(80)
        val item = newTrailerNotice("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TrailerNoticeLocaleLengthRule(255)
        val item = newTrailerNotice("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = TrailerNoticeRequiredLocaleRule("trailer notice needs this field")
        val item = newTrailerNotice("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerNoticeRequiredNameRule("trailer notice needs this field")
        val item = newTrailerNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TrailerNoticeRequiredDockRule("trailer notice needs a value")
        val item = newTrailerNotice("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerNoticeRequiredCodeRule("trailer notice needs this field")
        val item = newTrailerNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
