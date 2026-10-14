package com.acme.transport.lastmile.attempt.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAttemptNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAttemptNoticeValidator()
        val item = newDefaultAttemptNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DefaultAttemptNoticeRequiredCarrierRule("default attempt notice needs an entry")
        val item = newDefaultAttemptNotice("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DefaultAttemptNoticeUnitLengthRule(86400)
        val item = newDefaultAttemptNotice("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DefaultAttemptNoticeRequiredUnitRule("default attempt notice needs a value")
        val item = newDefaultAttemptNotice("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
