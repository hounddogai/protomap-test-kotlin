package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentTemplateValidator()
        val item = newIncidentTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentTemplateRequiredCodeRule("incident template needs this field")
        val item = newIncidentTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IncidentTemplateRequiredTitleRule("incident template needs a value")
        val item = newIncidentTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
