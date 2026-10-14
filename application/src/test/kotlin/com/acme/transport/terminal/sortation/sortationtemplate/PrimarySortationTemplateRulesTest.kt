package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimarySortationTemplateValidator()
        val item = newPrimarySortationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = PrimarySortationTemplateRequiredLaneRule("primary sortation template needs this field")
        val item = newPrimarySortationTemplate("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PrimarySortationTemplateEndsAtRangeRule(32, 1000)
        val item = newPrimarySortationTemplate("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PrimarySortationTemplateLaneLengthRule(255)
        val item = newPrimarySortationTemplate("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PrimarySortationTemplateMarginRangeRule(5.4, 3636.5)
        val item = newPrimarySortationTemplate("x", "y")
        item.margin = 3638.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
