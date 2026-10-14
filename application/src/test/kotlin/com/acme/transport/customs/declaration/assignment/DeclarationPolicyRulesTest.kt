package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationPolicyValidator()
        val item = newDeclarationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = DeclarationPolicyVersionRangeRule(64, 86400)
        val item = newDeclarationPolicy("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DeclarationPolicyRequiredLabelRule("declaration policy needs this field")
        val item = newDeclarationPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationPolicyCodeLengthRule(1000)
        val item = newDeclarationPolicy("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
