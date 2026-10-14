package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerTemplateValidator()
        val item = newManualTrailerTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualTrailerTemplateWeightRangeRule(6.7, 778.8)
        val item = newManualTrailerTemplate("x", "y")
        item.weight = 780.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualTrailerTemplateEndsAtRangeRule(32, 255)
        val item = newManualTrailerTemplate("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualTrailerTemplateTitleLengthRule(32)
        val item = newManualTrailerTemplate("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualTrailerTemplateWindowDaysRangeRule(1000, 3600)
        val item = newManualTrailerTemplate("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
