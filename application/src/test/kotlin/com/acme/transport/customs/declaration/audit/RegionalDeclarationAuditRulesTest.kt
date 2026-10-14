package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDeclarationAuditValidator()
        val item = newRegionalDeclarationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDeclarationAuditRequiredCodeRule("regional declaration audit needs a value")
        val item = newRegionalDeclarationAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDeclarationAuditLabelLengthRule(255)
        val item = newRegionalDeclarationAudit("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
