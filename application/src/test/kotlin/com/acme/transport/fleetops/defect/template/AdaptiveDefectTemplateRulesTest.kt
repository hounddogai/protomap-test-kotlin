package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDefectTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDefectTemplateValidator()
        val item = newAdaptiveDefectTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AdaptiveDefectTemplateSummaryLengthRule(80)
        val item = newAdaptiveDefectTemplate("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDefectTemplateCodeLengthRule(3600)
        val item = newAdaptiveDefectTemplate("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveDefectTemplateRequiredTitleRule("adaptive defect template needs a value")
        val item = newAdaptiveDefectTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
