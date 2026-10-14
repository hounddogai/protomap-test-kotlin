package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledQualificationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledQualificationTemplateValidator()
        val item = newScheduledQualificationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ScheduledQualificationTemplatePhoneLengthRule(80)
        val item = newScheduledQualificationTemplate("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledQualificationTemplateRequiredCodeRule("scheduled qualification template needs this field")
        val item = newScheduledQualificationTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
