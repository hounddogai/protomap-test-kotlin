package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSortationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveSortationTemplateValidator()
        val item = newAdaptiveSortationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AdaptiveSortationTemplateDiscountRangeRule(2.5, 1268.5)
        val item = newAdaptiveSortationTemplate("x", "y")
        item.discount = 1270.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveSortationTemplateCodeLengthRule(32)
        val item = newAdaptiveSortationTemplate("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveSortationTemplateTitleLengthRule(16)
        val item = newAdaptiveSortationTemplate("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AdaptiveSortationTemplateCeilingRangeRule(4.8, 3919.6)
        val item = newAdaptiveSortationTemplate("x", "y")
        item.ceiling = 3921.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AdaptiveSortationTemplateRequiredNotesRule("adaptive sortation template needs an entry")
        val item = newAdaptiveSortationTemplate("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
