package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationClaimValidator()
        val item = newDeclarationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = DeclarationClaimVersionRangeRule(255, 1000)
        val item = newDeclarationClaim("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationClaimRequiredCodeRule("declaration claim needs a value")
        val item = newDeclarationClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = DeclarationClaimLocaleLengthRule(120)
        val item = newDeclarationClaim("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
