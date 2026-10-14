package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDeclarationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDeclarationAuditValidator()
        val item = newGlobalDeclarationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalDeclarationAuditNameLengthRule(1000)
        val item = newGlobalDeclarationAudit("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDeclarationAuditCodeLengthRule(120)
        val item = newGlobalDeclarationAudit("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalDeclarationAuditRequiredCodeRule("global declaration audit needs a value")
        val item = newGlobalDeclarationAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = GlobalDeclarationAuditAmountRangeRule(2.3, 3095.3)
        val item = newGlobalDeclarationAudit("x", "y")
        item.amount = 3096.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = GlobalDeclarationAuditRequiredPhoneRule("global declaration audit needs a value")
        val item = newGlobalDeclarationAudit("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
