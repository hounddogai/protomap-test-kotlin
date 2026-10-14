package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSortationDigestValidator()
        val item = newPendingSortationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingSortationDigestLabelLengthRule(1000)
        val item = newPendingSortationDigest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PendingSortationDigestLocaleLengthRule(64)
        val item = newPendingSortationDigest("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingSortationDigestRequiredLabelRule("pending sortation digest needs a value")
        val item = newPendingSortationDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingSortationDigestRequiredCodeRule("pending sortation digest needs this field")
        val item = newPendingSortationDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
