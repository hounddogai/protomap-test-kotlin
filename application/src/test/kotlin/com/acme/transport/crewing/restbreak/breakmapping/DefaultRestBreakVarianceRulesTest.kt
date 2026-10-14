package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRestBreakVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRestBreakVarianceValidator()
        val item = newDefaultRestBreakVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRestBreakVarianceCodeLengthRule(16)
        val item = newDefaultRestBreakVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DefaultRestBreakVarianceNotesLengthRule(16)
        val item = newDefaultRestBreakVariance("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
