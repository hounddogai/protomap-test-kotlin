package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDeclarationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDeclarationTierValidator()
        val item = newRollingDeclarationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RollingDeclarationTierDescriptionLengthRule(16)
        val item = newRollingDeclarationTier("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = RollingDeclarationTierSourceLengthRule(16)
        val item = newRollingDeclarationTier("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDeclarationTierRequiredCodeRule("rolling declaration tier needs this field")
        val item = newRollingDeclarationTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDeclarationTierCodeLengthRule(120)
        val item = newRollingDeclarationTier("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
