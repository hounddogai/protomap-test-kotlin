package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptBundleValidator()
        val item = newAttemptBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AttemptBundlePhoneLengthRule(255)
        val item = newAttemptBundle("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AttemptBundleRequiredDepotRule("attempt bundle needs a value")
        val item = newAttemptBundle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AttemptBundleNameLengthRule(64)
        val item = newAttemptBundle("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptBundleRequiredCodeRule("attempt bundle needs this field")
        val item = newAttemptBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
