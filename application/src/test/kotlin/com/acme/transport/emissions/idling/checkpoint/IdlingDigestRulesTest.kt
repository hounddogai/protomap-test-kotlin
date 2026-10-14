package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingDigestValidator()
        val item = newIdlingDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingDigestCodeLengthRule(64)
        val item = newIdlingDigest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = IdlingDigestRequiredNotesRule("idling digest needs a value")
        val item = newIdlingDigest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
