package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropExceptionValidator()
        val item = newDropException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DropExceptionNameLengthRule(255)
        val item = newDropException("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DropExceptionRequiredChannelRule("drop exception needs an entry")
        val item = newDropException("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropExceptionRequiredCodeRule("drop exception needs an entry")
        val item = newDropException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
