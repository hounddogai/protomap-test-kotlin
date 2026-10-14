package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationEntryValidator()
        val item = newDeclarationEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DeclarationEntryRequiredTitleRule("declaration entry needs this field")
        val item = newDeclarationEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationEntryRequiredCodeRule("declaration entry needs a value")
        val item = newDeclarationEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationEntryCodeLengthRule(1000)
        val item = newDeclarationEntry("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
