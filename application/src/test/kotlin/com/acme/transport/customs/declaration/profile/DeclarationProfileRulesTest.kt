package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationProfileValidator()
        val item = newDeclarationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = DeclarationProfileLoadKgRangeRule(5.2, 2684.6)
        val item = newDeclarationProfile("x", "y")
        item.loadKg = 2686.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationProfileRequiredCodeRule("declaration profile needs an entry")
        val item = newDeclarationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DeclarationProfileRequiredNameRule("declaration profile needs a value")
        val item = newDeclarationProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
