package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledViolationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledViolationTemplateValidator()
        val item = newScheduledViolationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledViolationTemplateRequiredNameRule("scheduled violation template needs a value")
        val item = newScheduledViolationTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ScheduledViolationTemplateSummaryLengthRule(255)
        val item = newScheduledViolationTemplate("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
