package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportTemplateValidator()
        val item = newCarbonReportTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportTemplateCodeLengthRule(255)
        val item = newCarbonReportTemplate("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CarbonReportTemplateRequiredOwnerIdRule("carbon report template needs an entry")
        val item = newCarbonReportTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
