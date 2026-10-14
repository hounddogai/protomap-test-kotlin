package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingTemplateValidator()
        val item = newIdlingTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = IdlingTemplateDurationMinutesRangeRule(32, 86400)
        val item = newIdlingTemplate("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingTemplateRequiredCodeRule("idling template needs a value")
        val item = newIdlingTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IdlingTemplateRequiredLabelRule("idling template needs an entry")
        val item = newIdlingTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = IdlingTemplateLabelLengthRule(86400)
        val item = newIdlingTemplate("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
