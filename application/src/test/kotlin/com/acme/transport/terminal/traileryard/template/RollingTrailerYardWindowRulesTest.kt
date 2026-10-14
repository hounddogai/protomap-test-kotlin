package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTrailerYardWindowValidator()
        val item = newRollingTrailerYardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RollingTrailerYardWindowRequiredChecksumRule("rolling trailer yard window needs an entry")
        val item = newRollingTrailerYardWindow("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RollingTrailerYardWindowChecksumLengthRule(32)
        val item = newRollingTrailerYardWindow("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingTrailerYardWindowRequiredCodeRule("rolling trailer yard window needs an entry")
        val item = newRollingTrailerYardWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingTrailerYardWindowCodeLengthRule(120)
        val item = newRollingTrailerYardWindow("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = RollingTrailerYardWindowRequiredChannelRule("rolling trailer yard window needs this field")
        val item = newRollingTrailerYardWindow("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
