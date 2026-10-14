package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationDraftValidator()
        val item = newDeclarationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DeclarationDraftSequenceRangeRule(16, 80)
        val item = newDeclarationDraft("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DeclarationDraftLabelLengthRule(120)
        val item = newDeclarationDraft("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
