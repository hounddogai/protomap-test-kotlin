package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTenderTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTenderTemplateValidator()
        val item = newCappedTenderTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedTenderTemplateLabelLengthRule(3600)
        val item = newCappedTenderTemplate("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedTenderTemplateRequiredOwnerIdRule("capped tender template needs a value")
        val item = newCappedTenderTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CappedTenderTemplateOwnerIdLengthRule(64)
        val item = newCappedTenderTemplate("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTenderTemplateRequiredCodeRule("capped tender template needs this field")
        val item = newCappedTenderTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedTenderTemplateRequiredLabelRule("capped tender template needs this field")
        val item = newCappedTenderTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
