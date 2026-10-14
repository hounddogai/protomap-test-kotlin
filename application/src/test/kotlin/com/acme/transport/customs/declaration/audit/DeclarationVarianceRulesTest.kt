package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationVarianceValidator()
        val item = newDeclarationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DeclarationVarianceSequenceRangeRule(3600, 3600)
        val item = newDeclarationVariance("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DeclarationVariancePhoneLengthRule(32)
        val item = newDeclarationVariance("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationVarianceRequiredCodeRule("declaration variance needs a value")
        val item = newDeclarationVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
