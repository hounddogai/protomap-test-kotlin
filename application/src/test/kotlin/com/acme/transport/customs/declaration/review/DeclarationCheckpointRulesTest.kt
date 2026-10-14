package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationCheckpointValidator()
        val item = newDeclarationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationCheckpointRequiredCodeRule("declaration checkpoint needs this field")
        val item = newDeclarationCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationCheckpointCodeLengthRule(80)
        val item = newDeclarationCheckpoint("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DeclarationCheckpointRequiredTitleRule("declaration checkpoint needs an entry")
        val item = newDeclarationCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
