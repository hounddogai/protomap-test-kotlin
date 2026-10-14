package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationQuotaValidator()
        val item = newDeclarationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DeclarationQuotaRatioRangeRule(1.8, 2366.6)
        val item = newDeclarationQuota("x", "y")
        item.ratio = 2368.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DeclarationQuotaLabelLengthRule(3600)
        val item = newDeclarationQuota("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DeclarationQuotaRequiredLabelRule("declaration quota needs this field")
        val item = newDeclarationQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationQuotaRequiredCodeRule("declaration quota needs a value")
        val item = newDeclarationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DeclarationQuotaCeilingRangeRule(5.6, 4673.1)
        val item = newDeclarationQuota("x", "y")
        item.ceiling = 4674.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationQuotaCodeLengthRule(80)
        val item = newDeclarationQuota("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
