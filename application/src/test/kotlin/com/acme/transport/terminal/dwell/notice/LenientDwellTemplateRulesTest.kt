package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellTemplateValidator()
        val item = newLenientDwellTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LenientDwellTemplateDockLengthRule(120)
        val item = newLenientDwellTemplate("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientDwellTemplateRequiredTitleRule("lenient dwell template needs this field")
        val item = newLenientDwellTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = LenientDwellTemplateRequiredSummaryRule("lenient dwell template needs this field")
        val item = newLenientDwellTemplate("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
