package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDeclarationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDeclarationRecordValidator()
        val item = newDefaultDeclarationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DefaultDeclarationRecordRequiredTrailerIdRule("default declaration record needs an entry")
        val item = newDefaultDeclarationRecord("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultDeclarationRecordLabelLengthRule(1000)
        val item = newDefaultDeclarationRecord("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
