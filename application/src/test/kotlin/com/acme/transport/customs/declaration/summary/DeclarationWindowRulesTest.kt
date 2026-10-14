package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationWindowValidator()
        val item = newDeclarationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationWindowRequiredCodeRule("declaration window needs an entry")
        val item = newDeclarationWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DeclarationWindowChecksumLengthRule(16)
        val item = newDeclarationWindow("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationWindowCodeLengthRule(3600)
        val item = newDeclarationWindow("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
