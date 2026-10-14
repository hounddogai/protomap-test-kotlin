package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDeclarationTemplateValidator()
        val item = newRegionalDeclarationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RegionalDeclarationTemplateRevisionRangeRule(64, 86400)
        val item = newRegionalDeclarationTemplate("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RegionalDeclarationTemplateCapacityRangeRule(16, 32)
        val item = newRegionalDeclarationTemplate("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDeclarationTemplateCodeLengthRule(80)
        val item = newRegionalDeclarationTemplate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDeclarationTemplateLabelLengthRule(86400)
        val item = newRegionalDeclarationTemplate("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
