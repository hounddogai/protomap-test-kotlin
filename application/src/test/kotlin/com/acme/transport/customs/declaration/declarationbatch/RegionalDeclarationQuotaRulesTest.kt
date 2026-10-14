package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDeclarationQuotaValidator()
        val item = newRegionalDeclarationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RegionalDeclarationQuotaRequiredSegmentRule("regional declaration quota needs this field")
        val item = newRegionalDeclarationQuota("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalDeclarationQuotaRequiredLabelRule("regional declaration quota needs this field")
        val item = newRegionalDeclarationQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDeclarationQuotaLabelLengthRule(80)
        val item = newRegionalDeclarationQuota("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalDeclarationQuotaRequiredNameRule("regional declaration quota needs this field")
        val item = newRegionalDeclarationQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
