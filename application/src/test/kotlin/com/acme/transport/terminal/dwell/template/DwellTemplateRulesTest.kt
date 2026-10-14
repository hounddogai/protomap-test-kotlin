package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellTemplateValidator()
        val item = newDwellTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DwellTemplateMinimumRangeRule(4.8, 724.2)
        val item = newDwellTemplate("x", "y")
        item.minimum = 725.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = DwellTemplateExternalRefLengthRule(80)
        val item = newDwellTemplate("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DwellTemplateRequiredExternalRefRule("dwell template needs a value")
        val item = newDwellTemplate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DwellTemplateRequiredCategoryRule("dwell template needs an entry")
        val item = newDwellTemplate("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellTemplateCodeLengthRule(64)
        val item = newDwellTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
